import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SseBroadcaster {
    private static class Client {
        final HttpExchange exchange;
        final OutputStream os;
        Client(HttpExchange ex, OutputStream os) {
            this.exchange = ex;
            this.os = os;
        }
    }
    private final List<Client> clients = new CopyOnWriteArrayList<>();
    public void add(HttpExchange ex, OutputStream os) {
        clients.add(new Client(ex, os));
    }
    public void remove(HttpExchange ex) {
        clients.removeIf(c -> c.exchange == ex);
        try { ex.close(); } catch (Exception ignored) {}
    }
    public void broadcast(String data) {
        byte[] payload = ("data: " + data + "\n\n").getBytes(StandardCharsets.UTF_8);
        for (Client c : clients) {
            try {
                c.os.write(payload);
                c.os.flush();
            } catch (IOException e) {
                try { c.exchange.close(); } catch (Exception ignored) {}
                clients.remove(c);
            }
        }
    }
}
