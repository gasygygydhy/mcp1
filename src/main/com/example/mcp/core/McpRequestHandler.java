package main.com.example.mcp.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

public class McpRequestHandler {
    private final ToolsRegistry registry;
    private final ObjectMapper mapper = new ObjectMapper();
    public McpRequestHandler(ToolsRegistry registry) { this.registry = registry; }
    public String handle(String requestJson) {
        Long id = null;
        String method;
        JsonNode req;
        try {
            req = mapper.readTree(requestJson);
            id = req.has("id") && !req.get("id").isNull() ? req.get("id").asLong() : null;
            method = req.has("method") ? req.get("method").asText() : null;
            ObjectNode resp = mapper.createObjectNode();
            resp.put("jsonrpc", "2.0");
            if (id != null) resp.put("id", id);
            ObjectNode result;
            if ("initialize".equals(method)) {
                result = handleInitialize();
            } else if ("tools/list".equals(method)) {
                result = handleToolsList();
            } else if ("tools/call".equals(method)) {
                JsonNode params = req.path("params");
                result = handleToolsCall(params);
            } else {
                throw new RuntimeException("未知方法: " + method);
            }
            resp.set("result", result);
            return mapper.writeValueAsString(resp);
        } catch (Exception e) {
            try {
                ObjectNode err = mapper.createObjectNode();
                err.put("code", -32000);
                err.put("message", e.getMessage());
                ObjectNode resp = mapper.createObjectNode();
                resp.put("jsonrpc", "2.0");
                if (id != null) resp.put("id", id);
                resp.set("error", err);
                return mapper.writeValueAsString(resp);
            } catch (Exception ex) {
                return "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32000,\"message\":\"serialization error\"}}";
            }
        }
    }
    private ObjectNode handleInitialize() {
        ObjectNode r = mapper.createObjectNode();
        r.put("protocolVersion", "0.1.0");
        r.set("capabilities", mapper.createObjectNode());
        return r;
    }
    private ObjectNode handleToolsList() {
        ArrayNode tools = mapper.createArrayNode();
        for (McpTool t : registry.all().values()) {
            ObjectNode item = mapper.createObjectNode();
            item.put("name", t.getName());
            item.put("description", t.getDescription());
            item.set("inputSchema", mapper.valueToTree(t.getInputSchema()));
            tools.add(item);
        }
        ObjectNode r = mapper.createObjectNode();
        r.set("tools", tools);
        return r;
    }
    private ObjectNode handleToolsCall(JsonNode params) throws Exception {
        String name = params.path("name").asText();
        Map<String, Object> args = mapper.convertValue(params.path("arguments"), Map.class);
        if (args == null) args = Map.of();
        McpTool t = registry.get(name);
        if (t == null) throw new RuntimeException("工具不存在: " + name);
        Map<String, Object> result = t.execute(args);
        return mapper.valueToTree(result);
    }
}
