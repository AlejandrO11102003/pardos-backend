package com.pardos.pos.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key key;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        Key k = null;
        try {
            byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
            if (secretBytes.length < 32) {
                MessageDigest sha = MessageDigest.getInstance("SHA-256");
                secretBytes = sha.digest(secretBytes);
            }
            k = Keys.hmacShaKeyFor(secretBytes);
        } catch (Exception ex) {
            k = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        }
        this.key = k;
    }

    public String generateToken(Long userId, String role) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiresAt = new Date(now + 1000L * 60 * 60 * 24); // 24h
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        String sub = claims.getBody().getSubject();
        return Long.parseLong(sub);
    }

    public String getRoleFromToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            Object r = claims.getBody().get("role");
            return r != null ? r.toString() : null;
        } catch (Exception ex) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
