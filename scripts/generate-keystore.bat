@echo off
setlocal
set KEYSTORE=%~dp0..\keystore\mcp.p12
set STOREPASS=changeit
if not "%1"=="" set KEYSTORE=%1
if not "%2"=="" set STOREPASS=%2

if not exist "%~dp0..\keystore" mkdir "%~dp0..\keystore"

echo Generating PKCS12 keystore: %KEYSTORE%
where keytool >nul 2>nul
if errorlevel 1 (
  echo keytool not found. Please ensure JDK bin is in PATH.
  exit /b 1
)

keytool -genkeypair -alias mcp -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore "%KEYSTORE%" -validity 3650 -storepass %STOREPASS% -dname "CN=localhost"
if errorlevel 1 (
  echo Failed to generate keystore.
  exit /b 1
)

echo Done.
echo Use these environment variables to start HTTPS:
echo   set SSL_ENABLED=true
echo   set SERVER_PORT=8443
echo   set SSL_KEY_STORE=%KEYSTORE%
echo   set SSL_KEY_STORE_TYPE=PKCS12
echo   set SSL_KEY_STORE_PASSWORD=%STOREPASS%
endlocal
exit /b 0
