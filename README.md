# PTDB — Portal de Treinamento Dom Bosco

Aplicação web do Portal de Treinamento Dom Bosco (Engenharia de Software B — 2026/2).

O projeto une o trabalho de dois grupos: a parte 1 (RF06 a RF13) e a parte 2 (RF01 login, RF03 tela inicial e os campos de usuário e o MySQL do RF05). Todas as telas seguem o visual da parte 2: cabeçalho azul com o logo da faculdade e rodapé com FAQ e Ajuda.

## Stack

- Java 21+
- Spring Boot 3.3.5 (Web MVC e Data JPA)
- Front-end em HTML, CSS e JavaScript puros, servidos como arquivos estáticos e consumindo uma API JSON
- MySQL como banco padrão; H2 em memória para demonstrações
- Senhas com BCrypt (`spring-security-crypto`)
- Maven (via Maven Wrapper)

## Como executar

| Sistema | Como abrir |
| --- | --- |
| **Windows** | Dois cliques em `executar.bat` |
| **Mac** | Dois cliques em `executar.command` (abre o Terminal sozinho) |

Ao abrir, o script pergunta qual banco usar:

| Opção | Para quê |
| --- | --- |
| **1 - MySQL** | Uso normal: os dados ficam salvos no MySQL. Se não houver MySQL rodando, o script oferece trocar para o H2. |
| **2 - H2 em memória** | Testes e demonstrações em qualquer máquina, sem instalar MySQL. Os dados somem ao fechar a aplicação. |

Depois o script verifica o Java (baixa um JDK 21 portátil na pasta `.jdk` se não houver Java 21+), compila o projeto, sobe a aplicação e abre o navegador. A primeira execução demora mais, porque baixa o Java, o Maven e as dependências; precisa de internet. Para parar: `Ctrl + C` na janela do script, ou feche a janela. A porta padrão é 8080; pelo terminal dá para passar outra: `executar.bat 8081` ou `./executar.command 8081`.

### No Mac, na primeira vez

- **Aviso de segurança:** se o projeto foi baixado pela internet, o macOS pode bloquear o `executar.command` por ser de um "desenvolvedor não identificado". Nesse caso:
  - clique com o botão direito no arquivo → **Abrir** → **Abrir**;
  - no macOS 15 (Sequoia) ou mais novo: tente abrir uma vez e depois vá em **Ajustes do Sistema → Privacidade e Segurança** → **Abrir Mesmo Assim**.
- **Permissão de execução:** o arquivo já vai marcado como executável no Git. Isso se mantém ao baixar pelo GitHub (clone ou "Download ZIP"). Se o projeto for repassado num `.zip` criado no Windows, a marca se perde e o Mac diz que não tem permissão. Aí é preciso rodar uma vez no Terminal, dentro da pasta do projeto: `chmod +x executar.command`.

Sem o script, em qualquer sistema com Java 21+:

```bash
./mvnw spring-boot:run                                   # MySQL
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2     # H2 em memória
```

Acesse `http://localhost:8080/`. Sem login, o portal leva para a tela de login.

### Modo MySQL

Precisa de um MySQL 8 rodando na máquina. A configuração padrão é a da parte 2:

| Item | Valor padrão | Variável de ambiente para trocar |
| --- | --- | --- |
| Endereço | `localhost:3306`, base `portal_videos` | `PTDB_DB_URL` (URL JDBC completa) |
| Usuário | `root` | `PTDB_DB_USUARIO` |
| Senha | `123456` | `PTDB_DB_SENHA` |

A base `portal_videos` é criada automaticamente se não existir (`createDatabaseIfNotExist=true`). Ao iniciar, a aplicação roda o `schema.sql` (cria as tabelas que ainda não existem) e as cargas iniciais (usuários de teste, categorias e FAQ, só quando as tabelas estão vazias).

Se a opção MySQL for escolhida e não houver MySQL em `localhost:3306`, o script avisa e pergunta se deve usar o H2.

### Modo H2 em memória

Não precisa instalar nada além do Java. Cada execução começa com o banco vazio, recriado pelo mesmo `schema.sql` e pelas mesmas cargas iniciais. É o modo indicado para apresentar o projeto em outra máquina.

Console do H2: `http://localhost:8080/h2-console`, JDBC URL `jdbc:h2:mem:ptdb`, usuário `sa`, sem senha.

### Arquivos enviados

Vídeos e capas enviados como arquivo ficam na pasta `uploads/` (fora do banco, nos dois modos). A pasta está no `.gitignore`. O local pode ser trocado pela propriedade `ptdb.storage.location`.

## Login

A tela de login aceita **matrícula ou e-mail** e senha. Usuários criados na primeira execução:

| Perfil | Matrícula | E-mail | Senha |
| --- | --- | --- | --- |
| Administrador | 100001 | admin@dombosco.edu.br | admin123 |
| Avaliador | 100002 | rafael.lima@dombosco.edu.br | avaliador123 |
| Aluno | 100003 | bruno.alves@dombosco.edu.br | aluno123 |
| Aluno | 100004 | ana.souza@dombosco.edu.br | aluno123 |
| Aluno | 100005 | carla.mendes@dombosco.edu.br | aluno123 |

Abaixo do formulário fica o bloco **Simular login (provisório)**, que entra direto como um dos três perfis, sem senha, para testar as telas. Ele deve sair antes de publicar o portal (veja [Produção](#produção-o-que-fazer-antes-de-publicar)).

## Telas

| Tela | Arquivo | Quem acessa | Requisito |
| --- | --- | --- | --- |
| Login | `tela_login.html` | Qualquer pessoa | RF01 |
| Tela inicial: vídeos aprovados com busca e filtro por categoria | `tela_inicial.html` | Todos os perfis | RF03, RF13 |
| Página do vídeo: player, dados, favorito, visualizações e "Mais vídeos de <categoria>" | `tela_video.html?id=` | Todos os perfis | RF04, RF06, RF13 |
| Meus Favoritos | `tela_favoritos.html` | Aluno | RF07 |
| Meus Vídeos (aluno) / Gerenciar Vídeos (administrador) | `tela_gerenciar_videos.html` | Aluno e administrador | RF06 |
| Liberar Vídeos: fila, parecer, decisão e histórico | `tela_avaliacao.html` | Avaliador | RF08 |
| Painel administrativo | `tela_painel.html` | Administrador | RF09 |
| Categorias | `tela_categorias.html` | Administrador | RF09, RF13 |
| Gerenciar FAQ | `tela_gerenciar_faq.html` | Administrador | RF12 |
| Usuários: busca e promoção de aluno a avaliador | `tela_usuarios.html` | Administrador | extra |
| FAQ / Dúvidas Frequentes | `tela_faq.html` | Todos os perfis | RF12 |
| Ajuda | `tela_ajuda.html` | Todos os perfis | extra |
| Páginas de erro 403, 404 e 500 | `error/` | — | — |

### Layout comum

- **Cabeçalho:** logo, botões das telas do perfil e o menu **Perfil**, que mostra nome, perfil e o botão Sair.
- **Rodapé:** botões FAQ e Ajuda.

Os dois são montados pelo `js/comum.js`, usado por todas as telas menos a de login. Os botões do cabeçalho por perfil:

| Perfil | Botões |
| --- | --- |
| Aluno | Início, Meus Favoritos, Meus Vídeos |
| Avaliador | Início, Liberar Vídeos |
| Administrador | Início, Painel, Gerenciar Vídeos, Categorias, Gerenciar FAQ, Usuários |

## Regras principais

### Fluxo de um vídeo

1. **Aluno** envia um vídeo em "Meus Vídeos" → fica **Aguardando avaliação**. Não há limite de duração.
2. **Avaliador** decide, sempre com parecer:
   - **Aprovado:** o vídeo aparece na tela inicial;
   - **Reprovado:** o vídeo não é publicado;
   - **Ajustes solicitados:** o parecer aparece para o aluno em "Meus Vídeos". Ao editar e salvar, o vídeo volta para a fila.
3. **Administrador** cadastra ou edita vídeos já **aprovados**, sem passar pelo avaliador.
4. Só vídeos aprovados aparecem na tela inicial e podem ser favoritados.

O vídeo pode ser um **link** (YouTube, Vimeo e Google Drive tocam dentro da página; outros sites abrem em nova aba) ou um **arquivo** enviado.

### Visibilidade

- O aluno vê e mexe só nos próprios vídeos em "Meus Vídeos".
- O administrador vê e mexe em todos.
- Vídeos ainda não aprovados podem ser abertos pelo aluno que os enviou, pelos avaliadores e pelos administradores.

### Visualizações (RF04)

Cada acesso à página de um vídeo aprovado soma 1 em `qtd_views`. O número aparece só para administradores: na página do vídeo e na coluna "Views" de Gerenciar Vídeos.

### Promoção a avaliador

Na tela Usuários, o administrador pesquisa por nome, matrícula ou e-mail e promove um aluno a avaliador. O novo perfil vale a partir do próximo login da pessoa.

## Arquitetura

```
Navegador (HTML + CSS + JS)  --fetch JSON-->  Controllers REST (/api/...)  -->  Services  -->  Repositories (JPA)  -->  MySQL ou H2
```

- As telas ficam em `src/main/resources/static/`:
  - `tela_*.html` na raiz;
  - `css/estilo.css` para todas as telas e `css/tela_login.css` para o login;
  - `js/` com um arquivo por tela, mais o `comum.js`;
  - `img/` com o logo;
  - `error/` com as páginas de erro.
- Cada tela busca os dados na API e monta o HTML com JavaScript.
- A sessão é a do servidor (cookie de sessão). Dois interceptadores controlam o acesso:
  - `AutenticacaoInterceptor`: sem login, as telas vão para o login e a API responde 401;
  - `PerfilInterceptor`: com o perfil errado, a resposta é 403.

### Acesso por perfil

| Caminhos | Perfil |
| --- | --- |
| `tela_login.html`, `/login`, `/perfil`, `css/`, `js/`, `img/` | Qualquer pessoa |
| Demais telas, `/api/sessao`, `/api/videos/**`, `/api/categorias`, `/api/faq`, `/midia/**` | Usuário logado |
| `tela_favoritos.html`, `/api/favoritos/**` | Aluno |
| `tela_gerenciar_videos.html`, `/api/gestao/**` | Aluno e administrador |
| `tela_avaliacao.html`, `/api/avaliacao/**` | Avaliador |
| `tela_painel.html`, `tela_categorias.html`, `tela_gerenciar_faq.html`, `tela_usuarios.html`, `/api/admin/**` | Administrador |

### Rotas

| Método | Rota | Descrição |
| --- | --- | --- |
| GET | `/` e `/login` | Levam para a tela inicial e para o login |
| POST | `/login` | Login com matrícula ou e-mail e senha (JSON) |
| POST | `/perfil` | Simulação de login por perfil (provisório) |
| POST | `/sair` | Encerra a sessão |
| GET | `/api/sessao` | Nome e perfil do usuário logado |
| GET | `/api/videos` | Vídeos aprovados |
| GET | `/api/videos/{id}` | Dados de um vídeo e relacionados; conta a visualização |
| GET | `/api/categorias` | Categorias com a quantidade de vídeos aprovados |
| GET, POST | `/api/favoritos`, `/api/favoritos/{videoId}` | Lista os favoritos e marca/desmarca |
| GET, POST, DELETE | `/api/gestao/videos/**` | Meus Vídeos / Gerenciar Vídeos (envio com `multipart/form-data`) |
| GET, POST | `/api/avaliacao/fila`, `/api/avaliacao/historico`, `/api/avaliacao/{videoId}` | Fila, histórico e decisão do avaliador |
| GET | `/api/admin/painel` | Indicadores, atividades e fila |
| GET, POST, PUT, DELETE | `/api/admin/categorias/**` | Categorias |
| GET | `/api/faq` | Perguntas do FAQ |
| POST, PUT, DELETE | `/api/admin/faq/**` | Gerenciar FAQ |
| GET, POST | `/api/admin/usuarios`, `/api/admin/usuarios/{id}/promover` | Usuários e promoção a avaliador |
| GET | `/midia/videos/{nome}`, `/midia/capas/{nome}` | Arquivos enviados (respeitam a visibilidade) |

Erros de validação voltam com status 400 e um objeto `{campo: mensagem}`. Erros de regra voltam com `{erro: mensagem}`.

### Organização do código Java

```
security/   login, sessão, simulação de login e interceptadores de acesso
usuario/    Usuario, UsuarioRepository, UsuarioController (tela Usuários)
catalogo/   CatalogoController (vídeos da tela inicial, página do vídeo, categorias)
video/      Video, VideoService, VideoController (Meus Vídeos / Gerenciar Vídeos), MidiaController, LinkVideo
avaliacao/  Avaliacao, AvaliacaoService, AvaliacaoController
favorito/   Favorito, FavoritoService, FavoritoController
categoria/  Categoria, CategoriaService, CategoriaController
faq/        ItemFaq, FaqService, FaqController, FaqAdminController
painel/     PainelService, PainelController
config/     WebConfig (regras de acesso) e cargas iniciais
storage/    FileStorageService (gravação dos arquivos enviados)
common/     exceções e GlobalExceptionHandler
```

## Banco de dados (RF05 e RF10)

A estrutura está em `src/main/resources/schema.sql` (`CREATE TABLE IF NOT EXISTS`) e funciona no MySQL e no H2. O Hibernate não cria nem altera tabelas (`ddl-auto=none`): o script é a única fonte da estrutura. Qualquer coluna nova precisa ser colocada nele.

| Tabela | Conteúdo | Relacionamentos e regras |
| --- | --- | --- |
| `usuario` | nome, e-mail, matrícula, senha (hash) e perfil | e-mail e matrícula únicos; perfil só aceita ADMINISTRADOR, AVALIADOR ou ALUNO |
| `categoria` | nome, descrição e se está ativa | nome único; 1 categoria → N vídeos |
| `video` | título, descrição, autor, link ou arquivo, capa, data de publicação, status, `qtd_views` e se está ativo | chaves estrangeiras para `categoria` e para o usuário que cadastrou; status e origem só aceitam os valores previstos |
| `favorito` | aluno, vídeo e data | par (aluno, vídeo) único; apagar o vídeo ou o usuário apaga os favoritos em cascata |
| `avaliacao` | vídeo, avaliador, decisão, parecer e data | chaves estrangeiras para `video` e `usuario` |
| `faq` | pergunta, resposta e tema | — |

A exclusão de vídeo é lógica (`ativo = false`) e remove os favoritos do vídeo na mesma transação. As avaliações ficam como histórico.

Diferenças em relação ao diagrama do RF10:

- O vídeo tem também o status `AJUSTES_SOLICITADOS`, exigido pelo RF08.
- O vídeo tem campos extras: autor, capa e quem cadastrou (RF06) e `qtd_views` (RF04).
- A categoria tem campos extras: descrição e "ativa" (RF09).
- O "Aluno" do diagrama é a tabela `usuario` com perfil, que guarda também avaliadores e administradores.
- O favorito tem `id` próprio; a unicidade do par aluno + vídeo fica numa restrição.

## Produção: o que fazer antes de publicar

O projeto está pronto para rodar com um MySQL local e com o login próprio. Para colocar no servidor da faculdade, falta o seguinte.

### Banco de dados

1. **Servidor:** usar MySQL 8.0.16 ou mais novo, porque as restrições `CHECK` do `schema.sql` exigem essa versão.
2. **Base e usuário:**
   - criar a base `portal_videos` com `utf8mb4`;
   - criar um usuário próprio do portal, com permissão só nessa base (não usar `root`);
   - se a base for criada pelo DBA, tirar `createDatabaseIfNotExist=true` da URL.
3. **Credenciais:**
   - configurar `PTDB_DB_URL`, `PTDB_DB_USUARIO` e `PTDB_DB_SENHA` como variáveis de ambiente no servidor;
   - nunca gravar a senha real no `application.properties` nem no Git.
4. **Mudanças de estrutura:** o `schema.sql` só cria tabelas que não existem e não altera tabelas já criadas. Depois da primeira publicação, qualquer coluna nova precisa:
   - de um `ALTER TABLE` rodado à mão no banco de produção;
   - e de ser colocada também no `schema.sql`.
5. **Cargas iniciais:**
   - remover `CargaInicialUsuarios`, que cria usuários com senhas conhecidas;
   - revisar as categorias e o FAQ iniciais (`CargaInicialCategorias` e `CargaInicialFaq`).
6. **Backup (RS10):** fazer backup periódico do banco (por exemplo, `mysqldump` diário) **e** da pasta de uploads, onde ficam os arquivos de vídeo e as capas. Apontar `ptdb.storage.location` para uma pasta persistente do servidor.
7. **Modo H2:** não usar o perfil `h2` em produção. O console do H2 fica desligado no modo padrão.

### Login pelo portal da faculdade

Hoje o login confere matrícula ou e-mail e senha na tabela `usuario` (`security/AutenticacaoService.login`). Em produção, quem autentica é o portal da faculdade.

1. **Definir a integração com a TI da faculdade:**
   - o mecanismo: SSO via OpenID Connect/OAuth2, SAML ou uma API de autenticação;
   - quais dados chegam: matrícula, nome, e-mail e vínculo (aluno, professor, funcionário).
2. **Trocar a autenticação:**
   - a validação da senha em `AutenticacaoService.login` passa a ser a validação junto ao portal (credenciais ou token);
   - se o login for por redirecionamento (SSO), `LoginController` e a tela de login passam a ter só um botão "Entrar com o portal da faculdade", e o retorno do portal cria a sessão com `UsuarioSessao.definir`.
3. **Primeiro acesso:**
   - procurar o usuário pela matrícula na tabela `usuario`;
   - se não existir, criar com os dados do portal e perfil **Aluno**.
4. **Perfis:**
   - avaliadores continuam sendo definidos no PTDB, pela tela Usuários;
   - o primeiro administrador é definido direto no banco: `UPDATE usuario SET perfil = 'ADMINISTRADOR' WHERE matricula = '...';`.
5. **Senha local:** a coluna `senha_hash` deixa de ser usada. Removê-la (ou permitir `NULL`) no `schema.sql`, na entidade `Usuario` e com `ALTER TABLE` no banco.
6. **Remover a simulação de login:**
   - o formulário "Simular login (provisório)" em `tela_login.html`;
   - o `PerfilController.simularLogin` (`POST /perfil`);
   - o `/perfil` da lista de exceções em `WebConfig`.
7. **Sair:** se houver SSO, o `POST /sair` deve encerrar também a sessão no portal (logout único).

### Servidor web

1. **HTTPS (RS07):** publicar só com HTTPS, por proxy reverso ou pelas propriedades `server.ssl.*`, e ligar `server.servlet.session.cookie.secure=true`.
2. **Cookie de sessão:** ligar `server.servlet.session.cookie.same-site=strict` para proteger os formulários contra envio a partir de outros sites.
3. **Tamanho do upload:** revisar o limite de 512 MB (`spring.servlet.multipart.*`) conforme o espaço do servidor.

## Documentos

- Especificações funcionais da tela de login e da tela inicial: `docs/`.
