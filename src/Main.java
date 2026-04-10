public class Main {
    public static void main(String[] args) {
        try {
            if (args.length > 0 && "--stdio".equals(args[0])) {
                ToolsRegistry reg = ToolsRegistry.defaultRegistry();
                McpRequestHandler.runStdio(reg);
                return;
            }
            ToolsRegistry reg = ToolsRegistry.defaultRegistry();
            McpRequestHandler h = new McpRequestHandler(reg);
            String initReq = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{}}";
            String listReq = "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\",\"params\":{}}";
            String callReq = "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tools/call\",\"params\":{\"name\":\"echo\",\"arguments\":{\"text\":\"hello\"}}}";
            System.out.println(h.handle(initReq));
            System.out.println(h.handle(listReq));
            System.out.println(h.handle(callReq));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
