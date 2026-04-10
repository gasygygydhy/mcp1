import java.util.LinkedHashMap;
import java.util.Map;

public class ToolsRegistry {
    private final Map<String, McpTool> tools = new LinkedHashMap<>();
    public ToolsRegistry register(McpTool tool) {
        tools.put(tool.getName(), tool);
        return this;
    }
    public Map<String, McpTool> all() {
        return tools;
    }
    public McpTool get(String name) {
        return tools.get(name);
    }
    public static ToolsRegistry defaultRegistry() {
        return new ToolsRegistry()
                .register(new EchoTool())
                .register(new MySqlTool(
                        System.getenv("MYSQL_URL"),
                        System.getenv("MYSQL_USER"),
                        System.getenv("MYSQL_PASSWORD")
                ));
    }
}
