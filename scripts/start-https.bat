@echo off
setlocal
set SERVER_PORT=8443
set SSL_ENABLED=true
if "%SSL_KEY_STORE%"=="" (
  set SSL_KEY_STORE=%~dp0..\keystore\mcp.p12
)
if "%SSL_KEY_STORE_PASSWORD%"=="" (
  set SSL_KEY_STORE_PASSWORD=changeit
)
set SSL_KEY_STORE_TYPE=PKCS12

echo Starting Spring Boot with HTTPS on port %SERVER_PORT%
echo Using keystore: %SSL_KEY_STORE%

pushd "%~dp0.."
java -jar target\mcp1-0.1.0-SNAPSHOT.jar
popd
endlocal
exit /b 0
