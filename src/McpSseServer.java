import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class McpSseServer {
    private final ToolsRegistry registry;
    private final McpRequestHandler handler;
    private final SseBroadcaster broadcaster = new SseBroadcaster();
    private final ObjectMapper mapper = new ObjectMapper();
    private HttpServer server;
    public McpSseServer(ToolsRegistry registry) {
        this.registry = registry;
        this.handler = new McpRequestHandler(registry);
    }
    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/sse", new SseHandler());
        server.createContext("/rpc", new RpcHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("MCP SSE server started at http://localhost:" + port + " (/rpc, /sse)");
    }
    class SseHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
                ex.sendResponseHeaders(405, -1);
                ex.close();
                return;
            }
            Headers h = ex.getResponseHeaders();
            h.add("Content-Type", "text/event-stream; charset=utf-8");
            h.add("Cache-Control", "no-cache");
            h.add("Connection", "keep-alive");
            ex.sendResponseHeaders(200, 0);
            OutputStream os = ex.getResponseBody();
            os.write((":ok\n\n").getBytes(StandardCharsets.UTF_8));
            os.flush();
            broadcaster.add(ex, os);
        }
    }
    class RpcHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                ex.sendResponseHeaders(405, -1);
                ex.close();
                return;
            }
            String body = readAll(ex.getRequestBody());
            String respJson = handler.handle(body);
            byte[] bytes = respJson.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            ex.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(bytes);
            }
            broadcaster.broadcast(respJson);
        }
    }
    private static String readAll(InputStream is) throws IOException {
        byte[] buf = is.readAllBytes();
        return new String(buf, StandardCharsets.UTF_8);
    }
}
