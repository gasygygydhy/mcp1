public class McpServer {
    public static void main(String[] args) throws Exception {
        ToolsRegistry reg = ToolsRegistry.defaultRegistry();
        boolean useSse = false;
        int port = 8080;
        for (int i = 0; i < args.length; i++) {
            if ("--sse".equals(args[i])) useSse = true;
            if ("--port".equals(args[i]) && i + 1 < args.length) {
                try { port = Integer.parseInt(args[i + 1]); } catch (Exception ignored) {}
            }
        }
        if (useSse) {
            new McpSseServer(reg).start(port);
        } else {
            McpRequestHandler.runStdio(reg);
        }
    }
}
