package com.example.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.mcp.core.ToolsRegistry;
import com.example.mcp.core.McpRequestHandler;

@SpringBootApplication
public class McpApplication {
    @Bean
    public ToolsRegistry toolsRegistry() {
        return ToolsRegistry.defaultRegistry();
    }
    @Bean
    public McpRequestHandler mcpRequestHandler(ToolsRegistry registry) {
        return new McpRequestHandler(registry);
    }
    public static void main(String[] args) {
        SpringApplication.run(McpApplication.class, args);
    }
}
