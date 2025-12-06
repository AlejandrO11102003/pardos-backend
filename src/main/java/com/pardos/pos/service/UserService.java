package com.pardos.pos.service;

import com.pardos.pos.model.User;
import com.pardos.pos.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Optional<User> login(String username, String password) {
        Optional<User> maybe = userRepository.findByUsername(username);
        if (maybe.isEmpty()) return Optional.empty();
        User u = maybe.get();
        String stored = u.getPassword();
        if (stored == null) return Optional.empty();

        // First try BCrypt match (if stored as hash)
        try {
            if (passwordEncoder.matches(password, stored)) {
                return Optional.of(u);
            }
        } catch (Exception ex) {
            // fall back to plain-text comparison for legacy entries
        }

        // legacy: plain-text comparison
        if (stored.equals(password)) {
            // upgrade: hash the password and save
            try {
                u.setPassword(passwordEncoder.encode(password));
                userRepository.save(u);
            } catch (Exception ignored) {}
            return Optional.of(u);
        }
        return Optional.empty();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        // ensure password is hashed before saving
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    public Map<String, Object> migrateAllPasswords() {
        List<User> users = userRepository.findAll();
        int total = users.size();
        int migrated = 0;
        List<String> migratedUsers = new ArrayList<>();

        for (User u : users) {
            String stored = u.getPassword();
            if (stored == null || stored.isEmpty()) continue;
            // common bcrypt prefixes
            if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
                continue; // already hashed
            }
            try {
                u.setPassword(passwordEncoder.encode(stored));
                userRepository.save(u);
                migrated++;
                migratedUsers.add(u.getUsername());
            } catch (Exception ex) {
                // skip on error, but continue
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("migrated", migrated);
        result.put("users", migratedUsers);
        return result;
    }
    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User updateUser(Long id, User updated) {
        User existing = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!existing.getUsername().equals(updated.getUsername())) {
            if (userRepository.findByUsername(updated.getUsername()).isPresent()) {
                throw new RuntimeException("Username already exists");
            }
            existing.setUsername(updated.getUsername());
        }

        existing.setName(updated.getName());
        existing.setRole(updated.getRole());

        if (updated.getPassword() != null && !updated.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(updated.getPassword()));
        }

        return userRepository.save(existing);
    }
}
