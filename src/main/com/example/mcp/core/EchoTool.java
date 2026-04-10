package main.com.example.mcp.core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EchoTool implements McpTool {
    public String getName() { return "echo"; }
    public String getDescription() { return "回显传入的text参数"; }
    public Map<String, Object> getInputSchema() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("text", McpTool.schemaString("要回显的文本"));
        return McpTool.schemaObject(props, List.of("text"));
    }
    public Map<String, Object> execute(Map<String, Object> arguments) {
        Object t = arguments.get("text");
        String text = t == null ? "" : String.valueOf(t);
        return McpTool.resultWithText(text);
    }
}
