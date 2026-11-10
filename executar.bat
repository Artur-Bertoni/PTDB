@echo off

rem Roda dentro de "cmd /k" para a janela continuar aberta depois que o script termina
if not "%PTDB_JANELA%"=="1" (
    set "PTDB_JANELA=1"
    cmd /k call "%~f0" %*
    exit /b
)

setlocal EnableExtensions
title PTDB - Portal de Treinamento Dom Bosco

cd /d "%~dp0"

set "PORTA=%~1"
if "%PORTA%"=="" set "PORTA=8080"
set "JAVA_MIN=21"
set "JDK_DIR=%~dp0.jdk"
set "JAR=target\ptdb-0.0.1-SNAPSHOT.jar"
set "TMP_JAVA=%TEMP%\ptdb_java_%RANDOM%.txt"

echo %PORTA%| findstr /r "^[0-9][0-9]*$" >nul || (
    echo Porta invalida: "%PORTA%"
    echo Uso: executar.bat [porta]
    goto falha
)

:perguntar_banco
echo.
echo Qual banco de dados usar?
echo   1 - MySQL ^(precisa de um MySQL rodando nesta maquina^)
echo   2 - H2 em memoria ^(nao precisa instalar nada; os dados somem ao fechar^)
set "MODO="
set "ESCOLHA="
set /p "ESCOLHA=Digite 1 ou 2 e aperte Enter: "
if "%ESCOLHA%"=="1" set "MODO=mysql"
if "%ESCOLHA%"=="2" set "MODO=h2"
if not defined MODO (
    echo Opcao invalida.
    goto perguntar_banco
)

echo.
echo [1/4] Verificando Java %JAVA_MIN% ou superior...
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" call :checar_java "%JAVA_HOME%\bin\java.exe" "%JAVA_HOME%" && goto java_ok
set "JAVA_PATH="
for %%I in (java.exe) do set "JAVA_PATH=%%~$PATH:I"
if defined JAVA_PATH call :checar_java "%JAVA_PATH%" && goto java_ok
call :jdk_local && goto java_ok

echo       Java %JAVA_MIN%+ nao encontrado. Baixando JDK portatil (cerca de 200 MB, so na primeira vez)...
call :baixar_jdk || (
    echo Nao foi possivel baixar o JDK. Verifique a conexao com a internet
    echo ou instale manualmente o Java %JAVA_MIN%+: https://adoptium.net
    goto falha
)
call :jdk_local && goto java_ok
echo Falha ao preparar o JDK portatil em "%JDK_DIR%".
goto falha

:java_ok
set "JAVA_HOME=%JH%"
set "PATH=%JH%\bin;%PATH%"
echo       OK - Java %JV% em "%JH%"

echo.
echo [2/4] Verificando se a porta %PORTA% esta livre...
powershell -NoProfile -Command "if (Get-NetTCPConnection -LocalPort %PORTA% -State Listen -ErrorAction SilentlyContinue) { exit 1 }"
if errorlevel 1 (
    echo A porta %PORTA% ja esta em uso - provavelmente o PTDB ja esta rodando em outra janela.
    echo Feche a outra instancia ou use outra porta, ex.: executar.bat 8081
    goto falha
)
echo       OK

if "%MODO%"=="h2" goto banco_ok
if defined PTDB_DB_URL goto banco_ok
powershell -NoProfile -Command "if (-not (Get-NetTCPConnection -LocalPort 3306 -State Listen -ErrorAction SilentlyContinue)) { exit 1 }"
if not errorlevel 1 goto banco_ok
echo.
echo Nenhum MySQL encontrado em localhost:3306.
set "RESPOSTA="
set /p "RESPOSTA=Deseja usar o H2 em memoria? (S/N): "
if /i "%RESPOSTA%"=="S" set "MODO=h2"
if "%MODO%"=="h2" goto banco_ok
echo Inicie o MySQL ^(usuario root / senha 123456^) e rode o executar.bat de novo.
goto falha

:banco_ok
set "PERFIL="
if "%MODO%"=="h2" set "PERFIL=--spring.profiles.active=h2"

echo.
echo [3/4] Compilando o projeto (na primeira vez baixa Maven e dependencias, pode demorar)...
call "%~dp0mvnw.cmd" -B -q -DskipTests clean package
if errorlevel 1 (
    echo Falha na compilacao. Veja as mensagens acima.
    goto falha
)
if not exist "%JAR%" (
    echo Compilacao terminou, mas "%JAR%" nao foi gerado.
    goto falha
)
echo       OK - %JAR%

echo.
echo [4/4] Iniciando a aplicacao...
echo.
echo   Aplicacao : http://localhost:%PORTA%
if "%MODO%"=="h2" (
    echo   Banco H2  : em memoria, os dados somem ao fechar a aplicacao
    echo               console em http://localhost:%PORTA%/h2-console
    echo               JDBC URL jdbc:h2:mem:ptdb / usuario sa / senha vazia
) else (
    echo   Banco     : MySQL, base portal_videos em localhost:3306
)
echo   Para parar: Ctrl + C nesta janela
echo.

start "" /b powershell -NoProfile -Command "$ProgressPreference = 'SilentlyContinue'; for ($i = 0; $i -lt 90; $i++) { try { Invoke-WebRequest -UseBasicParsing -TimeoutSec 2 'http://localhost:%PORTA%/' | Out-Null; Start-Process 'http://localhost:%PORTA%/'; break } catch { Start-Sleep -Seconds 1 } }"

"%JH%\bin\java.exe" -jar "%JAR%" --server.port=%PORTA% %PERFIL%
set "SAIDA=%ERRORLEVEL%"
del "%TMP_JAVA%" >nul 2>&1
echo.
if "%SAIDA%"=="0" (
    echo A aplicacao foi encerrada.
) else if "%SAIDA%"=="130" (
    echo A aplicacao foi encerrada.
) else if "%SAIDA%"=="-1073741510" (
    echo A aplicacao foi encerrada.
) else (
    echo A aplicacao terminou com erro ^(codigo %SAIDA%^). Veja as mensagens acima.
)
endlocal & exit /b %SAIDA%

:checar_java
set "JV=0"
set "JH=%~2"
"%~1" -XshowSettings:properties -version > "%TMP_JAVA%" 2>&1 || exit /b 1
for /f "tokens=2 delims==" %%V in ('findstr /c:"java.specification.version =" "%TMP_JAVA%"') do set "JV=%%V"
set "JV=%JV: =%"
for /f "delims=." %%A in ("%JV%") do set "JV=%%A"
if not defined JH call :java_home_da_saida
if defined JH if not exist "%JH%\bin\java.exe" set "JH="
if not defined JH for %%P in ("%~dp1..") do set "JH=%%~fP"
if not exist "%JH%\bin\java.exe" exit /b 1
if %JV% GEQ %JAVA_MIN% exit /b 0
exit /b 1

:java_home_da_saida
for /f "tokens=2 delims==" %%H in ('findstr /c:"java.home =" "%TMP_JAVA%"') do set "JH=%%H"
if defined JH set "JH=%JH:~1%"
exit /b 0

:jdk_local
if not exist "%JDK_DIR%" exit /b 1
for /d %%D in ("%JDK_DIR%\*") do if exist "%%D\bin\java.exe" call :checar_java "%%D\bin\java.exe" "%%D" && exit /b 0
exit /b 1

:baixar_jdk
set "ARQ=x64"
if /i "%PROCESSOR_ARCHITECTURE%"=="ARM64" set "ARQ=aarch64"
set "JDK_URL=https://api.adoptium.net/v3/binary/latest/%JAVA_MIN%/ga/windows/%ARQ%/jdk/hotspot/normal/eclipse"
if not exist "%JDK_DIR%" mkdir "%JDK_DIR%"
curl -fL --retry 3 -o "%JDK_DIR%\jdk.zip" "%JDK_URL%" || exit /b 1
tar -xf "%JDK_DIR%\jdk.zip" -C "%JDK_DIR%" || exit /b 1
del "%JDK_DIR%\jdk.zip" >nul 2>&1
exit /b 0

:falha
del "%TMP_JAVA%" >nul 2>&1
echo.
endlocal & exit /b 1
