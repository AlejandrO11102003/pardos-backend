package com.pardos.pos.service;

import com.pardos.pos.model.Notification;
import com.pardos.pos.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final ExecutorService sseExecutor = Executors.newCachedThreadPool();

    public List<Notification> getAll() {
        return notificationRepository.findTop5ByOrderByCreatedAtDesc();
    }

    public Notification add(String message, String type) {
        Notification n = new Notification();
        n.setMessage(message);
        n.setType(type);
        n.setRead(false);
        n.setCreatedAt(java.time.Instant.now());
        Notification saved = notificationRepository.save(n);
        sendEvent(saved, "notification");
        sendEvent(notificationRepository.findTop5ByOrderByCreatedAtDesc(), "refresh");
        return saved;
    }

    public void markRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
            sendEvent(notificationRepository.findTop5ByOrderByCreatedAtDesc(), "refresh");
        });
    }

    public void clearAll() {
        notificationRepository.deleteAll();
        sendEvent(notificationRepository.findTop5ByOrderByCreatedAtDesc(), "refresh");
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));
        try {
            emitter.send(SseEmitter.event().name("refresh").data(notificationRepository.findTop5ByOrderByCreatedAtDesc()));
        } catch (Exception ex) {
        }

        return emitter;
    }

    private void sendEvent(Object data, String eventName) {
        for (SseEmitter emitter : emitters) {
            CompletableFuture.runAsync(() -> {
                try {
                    emitter.send(SseEmitter.event().name(eventName).data(data));
                } catch (Exception ex) {
                    // cliente desconectado o error de escritura,remover y completar el emitter
                    emitters.remove(emitter);
                    try {
                        emitter.complete();
                    } catch (Exception ignore) {}
                    log.debug("Removed SSE emitter after send failure: {}", ex.toString());
                }
            }, sseExecutor);
        }
    }

    @PreDestroy
    public void shutdown() {
        sseExecutor.shutdownNow();
    }
}
