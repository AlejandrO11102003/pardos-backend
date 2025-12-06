package com.pardos.pos.controller;

import com.pardos.pos.model.User;
import com.pardos.pos.service.UserService;
import com.pardos.pos.service.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        Optional<User> maybe = userService.login(username, password);
        if (maybe.isPresent()) {
            User u = maybe.get();
            u.setPassword(null);
            String token = jwtUtil.generateToken(u.getId(), u.getRole());
            return Map.of("token", token, "user", u);
        }
        return Map.of();
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                        @RequestBody Map<String, String> body) {
        // only allow admin users to create new users
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Map.of("error", "unauthorized");
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) return Map.of("error", "unauthorized");
        String role = jwtUtil.getRoleFromToken(token);
        if (role == null || !role.equalsIgnoreCase("admin")) {
            return Map.of("error", "forbidden");
        }

        User u = new User();
        u.setUsername(body.get("username"));
        u.setPassword(body.get("password"));
        u.setName(body.get("name"));
        u.setRole(body.getOrDefault("role", "cajero"));
        User created = userService.createUser(u);
        created.setPassword(null);
        return Map.of("user", created);
    }

    @PostMapping("/migrate-passwords")
    public Map<String, Object> migratePasswords(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // only allow admin users to run migration
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Map.of("error", "unauthorized");
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) return Map.of("error", "unauthorized");
        String role = jwtUtil.getRoleFromToken(token);
        if (role == null || !role.equalsIgnoreCase("admin")) {
            return Map.of("error", "forbidden");
        }

        Map<String, Object> result = userService.migrateAllPasswords();
        return result;
    }

    @GetMapping("/me")
    public Optional<User> me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return Optional.empty();
        String token = authHeader.substring(7);
        try {
            if (!jwtUtil.validateToken(token)) return Optional.empty();
            Long userId = jwtUtil.getUserIdFromToken(token);
            Optional<User> maybe = userService.findById(userId);
            maybe.ifPresent(u -> u.setPassword(null));
            return maybe;
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
