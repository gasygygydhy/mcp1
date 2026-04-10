package com.example.mcp.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class SseController {
    private final RpcController rpcController;
    public SseController(RpcController rpcController) {
        this.rpcController = rpcController;
    }
    @GetMapping("/sse")
    public SseEmitter sse() {
        return rpcController.hub().subscribe();
    }
}
