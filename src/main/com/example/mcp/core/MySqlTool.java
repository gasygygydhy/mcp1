package main.com.example.mcp.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MySqlTool implements McpTool {
    private final String url;
    private final String user;
    private final String password;
    public MySqlTool(String url, String user, String password) {
        this.url = url != null ? url : "jdbc:mysql://localhost:3307/test?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        this.user = user != null ? user : "root";
        this.password = password != null ? password : "root";
        try { Class.forName("com.mysql.cj.jdbc.Driver"); } catch (Exception ignored) {}
    }
    public String getName() { return "mysql_query"; }
    public String getDescription() { return "连接 MySQL 5.7 执行 SQL（默认连接 jdbc:mysql://localhost:3307/test 用户/密码 root/root）"; }
    public Map<String, Object> getInputSchema() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("sql", McpTool.schemaString("要执行的 SQL"));
        Map<String, Object> maxRows = new LinkedHashMap<>();
        maxRows.put("type", "integer");
        maxRows.put("description", "最多返回的行数（仅对查询有效，默认50）");
        props.put("maxRows", maxRows);
        return McpTool.schemaObject(props, List.of("sql"));
    }
    public Map<String, Object> execute(Map<String, Object> arguments) throws Exception {
        String sql = String.valueOf(arguments.get("sql"));
        int limit = 50;
        Object mr = arguments.get("maxRows");
        if (mr != null) {
            try { limit = Integer.parseInt(String.valueOf(mr)); } catch (Exception ignored) {}
        }
        try (Connection conn = DriverManager.getConnection(this.url, this.user, this.password);
             Statement st = conn.createStatement()) {
            boolean isResultSet = st.execute(sql);
            if (isResultSet) {
                try (ResultSet rs = st.getResultSet()) {
                    StringBuilder sb = new StringBuilder();
                    ResultSetMetaData md = rs.getMetaData();
                    int cols = md.getColumnCount();
                    for (int i = 1; i <= cols; i++) {
                        if (i > 1) sb.append('\t');
                        sb.append(md.getColumnLabel(i));
                    }
                    sb.append('\n');
                    int count = 0;
                    while (rs.next() && count < limit) {
                        for (int i = 1; i <= cols; i++) {
                            if (i > 1) sb.append('\t');
                            Object val = rs.getObject(i);
                            sb.append(val == null ? "NULL" : String.valueOf(val));
                        }
                        sb.append('\n');
                        count++;
                    }
                    if (!rs.isAfterLast()) {
                        sb.append("... truncated to ").append(limit).append(" rows\n");
                    }
                    return McpTool.resultWithText(sb.toString());
                }
            } else {
                int updated = st.getUpdateCount();
                return McpTool.resultWithText("OK, updated=" + updated);
            }
        }
    }
}
