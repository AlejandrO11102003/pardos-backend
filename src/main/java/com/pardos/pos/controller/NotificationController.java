package com.pardos.pos.controller;

import com.pardos.pos.model.Notification;
import com.pardos.pos.service.NotificationService;
import com.pardos.pos.service.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public List<Notification> getAll() {
        return notificationService.getAll();
    }

    @PostMapping
    public Notification add(@RequestBody Map<String, String> body) {
        return notificationService.add(body.get("message"), body.getOrDefault("type", "info"));
    }

    @PutMapping("/{id}/read")
    public void markRead(@PathVariable Long id) {
        notificationService.markRead(id);
    }

    @DeleteMapping
    public void clearAll() {
        notificationService.clearAll();
    }

    @GetMapping("/stream")
    public SseEmitter stream(@RequestParam(value = "token", required = false) String token) {
        if (token != null && !jwtUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid token");
        }
        return notificationService.subscribe();
    }
}
