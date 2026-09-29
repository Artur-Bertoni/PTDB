@echo off
setlocal EnableExtensions
title PTDB - Portal de Treinamento Dom Bosco

rem ============================================================
rem  PTDB - compila e executa a aplicacao
rem
rem  Uso:  executar.bat [porta]          (porta padrao: 8080)
rem
rem  Requisitos: Windows 10 ou superior e internet no primeiro uso.
rem  Java e Maven NAO precisam estar instalados:
rem    - se nao houver Java 21+, um JDK portatil e baixado em .jdk\
rem    - o Maven e baixado automaticamente pelo Maven Wrapper (mvnw.cmd)
rem ============================================================

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

rem ---------------------------------------------------------------
echo.
echo [1/4] Verificando Java %JAVA_MIN% ou superior...
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" call :checar_java "%JAVA_HOME%\bin\java.exe" && goto java_ok
where java >nul 2>&1 && call :checar_java java && goto java_ok
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

rem ---------------------------------------------------------------
echo.
echo [2/4] Verificando se a porta %PORTA% esta livre...
powershell -NoProfile -Command "if (Get-NetTCPConnection -LocalPort %PORTA% -State Listen -ErrorAction SilentlyContinue) { exit 1 }"
if errorlevel 1 (
    echo A porta %PORTA% ja esta em uso - provavelmente o PTDB ja esta rodando em outra janela.
    echo Feche a outra instancia ou use outra porta, ex.: executar.bat 8081
    goto falha
)
echo       OK

rem ---------------------------------------------------------------
echo.
echo [3/4] Compilando o projeto (na primeira vez baixa Maven e dependencias, pode demorar)...
call "%~dp0mvnw.cmd" -B -q -DskipTests package
if errorlevel 1 (
    echo Falha na compilacao. Veja as mensagens acima.
    goto falha
)
if not exist "%JAR%" (
    echo Compilacao terminou, mas "%JAR%" nao foi gerado.
    goto falha
)
echo       OK - %JAR%

rem ---------------------------------------------------------------
echo.
echo [4/4] Iniciando a aplicacao...
echo.
echo   Aplicacao : http://localhost:%PORTA%
echo   Banco H2  : http://localhost:%PORTA%/h2-console
echo               JDBC URL jdbc:h2:mem:ptdb / usuario sa / senha vazia
echo   Para parar: Ctrl + C nesta janela
echo.

rem Abre o navegador assim que a aplicacao responder (ate 90 s).
start "" /b powershell -NoProfile -WindowStyle Hidden -Command "for ($i = 0; $i -lt 90; $i++) { try { Invoke-WebRequest -UseBasicParsing -TimeoutSec 2 'http://localhost:%PORTA%/' | Out-Null; Start-Process 'http://localhost:%PORTA%/'; break } catch { Start-Sleep -Seconds 1 } }"

"%JH%\bin\java.exe" -jar "%JAR%" --server.port=%PORTA%
set "SAIDA=%ERRORLEVEL%"
del "%TMP_JAVA%" >nul 2>&1
endlocal & exit /b %SAIDA%


rem ===============================================================
rem  Sub-rotinas
rem ===============================================================

rem Verifica se o java.exe informado e da versao minima.
rem Define JV (versao) e JH (java.home). Retorna 0 se atende.
:checar_java
set "JV=0"
set "JH="
"%~1" -XshowSettings:properties -version > "%TMP_JAVA%" 2>&1 || exit /b 1
for /f "tokens=2 delims==" %%V in ('findstr /c:"java.specification.version =" "%TMP_JAVA%"') do set "JV=%%V"
for /f "tokens=2 delims==" %%H in ('findstr /c:"java.home =" "%TMP_JAVA%"') do set "JH=%%H"
set "JV=%JV: =%"
if defined JH set "JH=%JH:~1%"
for /f "delims=." %%A in ("%JV%") do set "JV=%%A"
if not defined JH exit /b 1
if %JV% GEQ %JAVA_MIN% exit /b 0
exit /b 1

rem Procura um JDK portatil ja baixado em .jdk\
:jdk_local
if not exist "%JDK_DIR%" exit /b 1
for /d %%D in ("%JDK_DIR%\*") do if exist "%%D\bin\java.exe" call :checar_java "%%D\bin\java.exe" && exit /b 0
exit /b 1

rem Baixa e extrai o JDK 21 (Eclipse Temurin) em .jdk\
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
pause
endlocal & exit /b 1
