import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

public interface McpTool {
    String getName();
    String getDescription();
    Map<String, Object> getInputSchema();
    Map<String, Object> execute(Map<String, Object> arguments) throws Exception;
    static Map<String, Object> schemaObject(Map<String, Object> props, List<String> required) {
        Map<String, Object> o = new LinkedHashMap<>();
        o.put("type", "object");
        o.put("properties", props);
        if (required != null && !required.isEmpty()) o.put("required", required);
        return o;
    }
    static Map<String, Object> schemaString(String description) {
        Map<String, Object> o = new LinkedHashMap<>();
        o.put("type", "string");
        if (description != null) o.put("description", description);
        return o;
    }
    static Map<String, Object> textContent(String text) {
        Map<String, Object> t = new LinkedHashMap<>();
        t.put("type", "text");
        t.put("text", text);
        return t;
    }
    static Map<String, Object> resultWithText(String text) {
        Map<String, Object> r = new LinkedHashMap<>();
        List<Map<String, Object>> content = new ArrayList<>();
        content.add(textContent(text));
        r.put("content", content);
        return r;
    }
}
