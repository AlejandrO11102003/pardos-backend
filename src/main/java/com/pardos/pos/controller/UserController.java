package com.pardos.pos.controller;

import com.pardos.pos.model.User;
import com.pardos.pos.service.UserService;
import com.pardos.pos.service.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private boolean isAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return false;
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) return false;
        String role = jwtUtil.getRoleFromToken(token);
        return role != null && role.equalsIgnoreCase("admin");
    }

    @GetMapping
    public ResponseEntity<?> getAll(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!isAdmin(authHeader)) return ResponseEntity.status(403).body(Map.of("error", "forbidden"));
        
        List<User> users = userService.findAll();
        users.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                    @PathVariable Long id) {
        if (!isAdmin(authHeader)) return ResponseEntity.status(403).body(Map.of("error", "forbidden"));

        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                    @PathVariable Long id,
                                    @RequestBody User user) {
        if (!isAdmin(authHeader)) return ResponseEntity.status(403).body(Map.of("error", "forbidden"));
        try {
            User updated = userService.updateUser(id, user);
            updated.setPassword(null);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
