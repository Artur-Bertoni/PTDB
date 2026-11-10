#!/bin/bash

cd "$(dirname "$0")" || exit 1

PORTA="${1:-8080}"
JAVA_MIN=21
JDK_DIR="$PWD/.jdk"
JAR="target/ptdb-0.0.1-SNAPSHOT.jar"

falha() {
    echo
    read -r -p "Pressione Enter para fechar..." _
    exit 1
}

porta_em_uso() {
    (exec 3<>"/dev/tcp/127.0.0.1/$1") 2>/dev/null
}

versao_java() {
    "$1" -XshowSettings:properties -version 2>&1 | grep "java.specification.version" | sed "s/.*= *//" | cut -d. -f1
}

testar_java() {
    if [ ! -x "$1/bin/java" ]; then
        return 1
    fi
    local versao
    versao=$(versao_java "$1/bin/java")
    if [ -n "$versao" ] && [ "$versao" -ge "$JAVA_MIN" ]; then
        JAVA_HOME="$1"
        JV="$versao"
        return 0
    fi
    return 1
}

encontrar_java() {
    if [ -n "$JAVA_HOME" ] && testar_java "$JAVA_HOME"; then
        return 0
    fi
    if [ -x /usr/libexec/java_home ]; then
        local pasta
        pasta=$(/usr/libexec/java_home -v "$JAVA_MIN+" 2>/dev/null)
        if [ -n "$pasta" ] && testar_java "$pasta"; then
            return 0
        fi
    fi
    for pasta in "$JDK_DIR"/*/Contents/Home "$JDK_DIR"/*; do
        if testar_java "$pasta"; then
            return 0
        fi
    done
    return 1
}

baixar_java() {
    local sistema="mac"
    if [ "$(uname)" != "Darwin" ]; then
        sistema="linux"
    fi
    local arquitetura="x64"
    if [ "$(uname -m)" = "arm64" ] || [ "$(uname -m)" = "aarch64" ]; then
        arquitetura="aarch64"
    fi
    mkdir -p "$JDK_DIR"
    curl -fL --retry 3 -o "$JDK_DIR/jdk.tar.gz" \
        "https://api.adoptium.net/v3/binary/latest/$JAVA_MIN/ga/$sistema/$arquitetura/jdk/hotspot/normal/eclipse" || return 1
    tar -xzf "$JDK_DIR/jdk.tar.gz" -C "$JDK_DIR" || return 1
    rm -f "$JDK_DIR/jdk.tar.gz"
}

echo "PTDB - Portal de Treinamento Dom Bosco"

case "$PORTA" in
    ''|*[!0-9]*)
        echo "Porta invalida: \"$PORTA\""
        falha
        ;;
esac

MODO=""
while [ -z "$MODO" ]; do
    echo
    echo "Qual banco de dados usar?"
    echo "  1 - MySQL (precisa de um MySQL rodando nesta maquina)"
    echo "  2 - H2 em memoria (nao precisa instalar nada; os dados somem ao fechar)"
    read -r -p "Digite 1 ou 2 e aperte Enter: " ESCOLHA || falha
    if [ "$ESCOLHA" = "1" ]; then
        MODO="mysql"
    elif [ "$ESCOLHA" = "2" ]; then
        MODO="h2"
    else
        echo "Opcao invalida."
    fi
done

echo
echo "[1/4] Verificando Java $JAVA_MIN ou superior..."
if ! encontrar_java; then
    echo "      Java $JAVA_MIN+ nao encontrado. Baixando JDK portatil (cerca de 200 MB, so na primeira vez)..."
    if ! baixar_java; then
        echo "Nao foi possivel baixar o JDK. Verifique a conexao com a internet"
        echo "ou instale manualmente o Java $JAVA_MIN+: https://adoptium.net"
        falha
    fi
    if ! encontrar_java; then
        echo "Falha ao preparar o JDK portatil em \"$JDK_DIR\"."
        falha
    fi
fi
export JAVA_HOME
echo "      OK - Java $JV em \"$JAVA_HOME\""

echo
echo "[2/4] Verificando se a porta $PORTA esta livre..."
if porta_em_uso "$PORTA"; then
    echo "A porta $PORTA ja esta em uso - provavelmente o PTDB ja esta rodando em outra janela."
    echo "Feche a outra instancia e tente de novo."
    falha
fi
echo "      OK"

if [ "$MODO" = "mysql" ] && [ -z "$PTDB_DB_URL" ] && ! porta_em_uso 3306; then
    echo
    echo "Nenhum MySQL encontrado em localhost:3306."
    read -r -p "Deseja usar o H2 em memoria? (S/N): " RESPOSTA || falha
    if [ "$RESPOSTA" = "S" ] || [ "$RESPOSTA" = "s" ]; then
        MODO="h2"
    else
        echo "Inicie o MySQL (usuario root / senha 123456) e rode o executar.command de novo."
        falha
    fi
fi

PERFIL=""
if [ "$MODO" = "h2" ]; then
    PERFIL="--spring.profiles.active=h2"
fi

echo
echo "[3/4] Compilando o projeto (na primeira vez baixa Maven e dependencias, pode demorar)..."
if ! sh ./mvnw -B -q -DskipTests package; then
    echo "Falha na compilacao. Veja as mensagens acima."
    falha
fi
if [ ! -f "$JAR" ]; then
    echo "Compilacao terminou, mas \"$JAR\" nao foi gerado."
    falha
fi
echo "      OK - $JAR"

echo
echo "[4/4] Iniciando a aplicacao..."
echo
echo "  Aplicacao : http://localhost:$PORTA"
if [ "$MODO" = "h2" ]; then
    echo "  Banco H2  : em memoria, os dados somem ao fechar a aplicacao"
    echo "              console em http://localhost:$PORTA/h2-console"
    echo "              JDBC URL jdbc:h2:mem:ptdb / usuario sa / senha vazia"
else
    echo "  Banco     : MySQL, base portal_videos em localhost:3306"
fi
echo "  Para parar: Ctrl + C nesta janela"
echo

(
    for i in $(seq 1 90); do
        if curl -s -o /dev/null "http://localhost:$PORTA/"; then
            if command -v open >/dev/null; then
                open "http://localhost:$PORTA/"
            elif command -v xdg-open >/dev/null; then
                xdg-open "http://localhost:$PORTA/"
            fi
            break
        fi
        sleep 1
    done
) &

"$JAVA_HOME/bin/java" -jar "$JAR" --server.port="$PORTA" $PERFIL
