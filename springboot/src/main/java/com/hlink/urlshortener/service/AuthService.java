package com.hlink.urlshortener.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final SecretKey secretKey;

    // 간단한 관리자 계정 (실제 운영 환경에서는 DB에 저장)
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    public AuthService(PasswordEncoder passwordEncoder, @Value("${app.jwt.secret:hlink-secret-key-for-jwt-token-generation-minimum-256-bits-required-for-security}") String jwtSecret) {
        this.passwordEncoder = passwordEncoder;
        try {
            byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length < 32) {
                log.error("JWT secret key is too short. Minimum 32 bytes required, got: {}", keyBytes.length);
                throw new IllegalArgumentException("JWT secret key must be at least 32 bytes (256 bits)");
            }
            this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            log.error("Failed to create JWT secret key", e);
            throw new RuntimeException("Failed to initialize JWT secret key", e);
        }
    }

    public String authenticate(String username, String password) {
        // 간단한 인증 (실제 운영 환경에서는 DB에서 조회)
        if (!ADMIN_USERNAME.equals(username)) {
            log.warn("Invalid username: {}", username);
            throw new IllegalArgumentException("Invalid credentials");
        }
        
        // 평문 비밀번호 비교 (개발용 - 실제 운영 환경에서는 BCrypt 해시 사용)
        if (!ADMIN_PASSWORD.equals(password)) {
            log.warn("Invalid password for user: {}", username);
            throw new IllegalArgumentException("Invalid credentials");
        }
        
        log.info("User {} authenticated successfully", username);
        return generateToken(username);
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("role", "ADMIN");

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000)) // 24시간
                .signWith(secretKey)
                .compact();
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return null;
        }
    }
}

