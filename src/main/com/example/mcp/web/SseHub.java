package main.com.example.mcp.web;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SseHub {
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        try { emitter.send(SseEmitter.event().data(":ok")); } catch (IOException ignored) {}
        return emitter;
    }
    public void broadcast(String data) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().data(data));
            } catch (Exception e) {
                emitters.remove(emitter);
            }
        }
    }
}
