@echo off
setlocal
set PORT=8443

echo Initialize
curl --http2 -k -s -X POST https://localhost:%PORT%/rpc -H "Content-Type: application/json" -d "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{}}"
echo.

echo List tools
curl --http2 -k -s -X POST https://localhost:%PORT%/rpc -H "Content-Type: application/json" -d "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\",\"params\":{}}"
echo.

echo Echo
curl --http2 -k -s -X POST https://localhost:%PORT%/rpc -H "Content-Type: application/json" -d "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tools/call\",\"params\":{\"name\":\"echo\",\"arguments\":{\"text\":\"hello http2\"}}}"
echo.

echo SSE (press Ctrl+C to quit)
curl --http2 -k --no-buffer https://localhost:%PORT%/sse

endlocal
exit /b 0
