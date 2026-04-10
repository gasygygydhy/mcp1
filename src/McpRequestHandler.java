import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class McpRequestHandler {
    private final ToolsRegistry registry;
    private final ObjectMapper mapper = new ObjectMapper();
    public McpRequestHandler(ToolsRegistry registry) {
        this.registry = registry;
    }
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
    public static void runStdio(ToolsRegistry registry) throws IOException {
        McpRequestHandler h = new McpRequestHandler(registry);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintStream out = System.out;
        String line;
        StringBuilder buf = new StringBuilder();
        while ((line = br.readLine()) != null) {
            String s = line.trim();
            if (s.isEmpty()) continue;
            buf.append(s);
            if (balanced(buf)) {
                String req = buf.toString();
                buf.setLength(0);
                String resp = h.handle(req);
                out.println(resp);
                out.flush();
            } else {
                buf.append("\n");
            }
        }
    }
    private static boolean balanced(CharSequence s) {
        int depth = 0;
        boolean inStr = false;
        boolean esc = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inStr) {
                if (esc) {
                    esc = false;
                } else if (c == '\\') {
                    esc = true;
                } else if (c == '"') {
                    inStr = false;
                }
                continue;
            }
            if (c == '"') inStr = true;
            else if (c == '{') depth++;
            else if (c == '}') depth--;
        }
        return depth == 0 && !inStr;
    }
}
