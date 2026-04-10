package main.com.example.mcp.web;

import main.com.example.mcp.core.McpRequestHandler;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RpcController {
    private final McpRequestHandler handler;
    private final SseHub hub;
    public RpcController(McpRequestHandler handler) {
        this.handler = handler;
        this.hub = new SseHub();
    }
    @PostMapping(value = "/rpc", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> rpc(@RequestBody String body) {
        String resp = handler.handle(body);
        hub.broadcast(resp);
        return ResponseEntity.ok(resp);
    }
    public SseHub hub() { return hub; }
}
