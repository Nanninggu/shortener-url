package com.hlink.urlshortener.service;

import com.hlink.urlshortener.model.User;
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
import java.util.Optional;

@Slf4j
@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final SecretKey secretKey;

    // 간단한 관리자 계정 (실제 운영 환경에서는 DB에 저장)
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, @Value("${app.jwt.secret:hlink-secret-key-for-jwt-token-generation-minimum-256-bits-required-for-security}") String jwtSecret) {
        this.userService = userService;
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

    public AuthResult authenticate(String username, String password) {
        // 하드코딩된 관리자 계정을 먼저 확인 (DB 연결 실패 시에도 작동하도록)
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            log.info("Admin user {} authenticated successfully (hardcoded)", username);
            
            Long adminUserId = 0L; // 기본값
            
            // DB에 admin 계정이 없으면 생성 시도 (DB 연결 실패해도 로그인은 허용)
            try {
                Optional<User> adminUserOpt = userService.findByUsername(ADMIN_USERNAME);
                
                if (adminUserOpt.isEmpty()) {
                    // admin 계정 자동 생성
                    try {
                        com.hlink.urlshortener.dto.UserCreateRequest adminRequest = 
                            new com.hlink.urlshortener.dto.UserCreateRequest();
                        adminRequest.setUsername(ADMIN_USERNAME);
                        adminRequest.setPassword(ADMIN_PASSWORD);
                        adminRequest.setEmail("admin@hlink.com");
                        User adminUser = userService.createUser(adminRequest);
                        adminUserId = adminUser.getId();
                        
                        // 생성된 admin 계정의 role을 ADMIN으로 업데이트
                        userService.updateUserRole(adminUserId, "ADMIN");
                        log.info("Admin user created automatically with ID: {} and ADMIN role", adminUserId);
                    } catch (Exception e) {
                        log.warn("Failed to create admin user automatically: {}", e.getMessage(), e);
                        // 계정 생성 실패해도 로그인은 허용 (하드코딩된 계정)
                    }
                } else {
                    User adminUser = adminUserOpt.get();
                    adminUserId = adminUser.getId();
                    
                    // 기존 admin 계정의 role이 USER이면 ADMIN으로 업데이트
                    if (!"ADMIN".equals(adminUser.getRole())) {
                        try {
                            userService.updateUserRole(adminUserId, "ADMIN");
                            log.info("Updated existing admin user role from {} to ADMIN", adminUser.getRole());
                        } catch (Exception e) {
                            log.warn("Failed to update admin user role: {}", e.getMessage(), e);
                            // role 업데이트 실패해도 계속 진행
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Error accessing database for admin user (continuing with hardcoded auth): {}", e.getMessage());
                // DB 접근 실패해도 하드코딩된 계정으로 로그인 허용
            }
            
            try {
                return new AuthResult(generateToken(username, "ADMIN", adminUserId), adminUserId, username, "ADMIN");
            } catch (Exception e) {
                log.error("Error generating token for admin user: {}", e.getMessage(), e);
                throw new RuntimeException("인증 중 오류가 발생했습니다: " + e.getMessage(), e);
            }
        }
        
        // 일반 사용자 인증 (DB에서 조회)
        try {
            Optional<User> userOpt = null;
            try {
                userOpt = userService.findByUsername(username);
            } catch (Exception e) {
                log.error("Error finding user by username {}: {}", username, e.getMessage(), e);
                // DB 조회 실패 시 인증 실패로 처리
                throw new IllegalArgumentException("Invalid credentials");
            }
            
            if (userOpt == null || userOpt.isEmpty()) {
                log.warn("User not found: {}", username);
                throw new IllegalArgumentException("Invalid credentials");
            }
            
            User user = userOpt.get();
            
            // 계정 활성화 여부 확인
            if (user.getEnabled() != null && !user.getEnabled()) {
                log.warn("Account is disabled for user: {}", username);
                throw new IllegalArgumentException("계정이 비활성화되어 있습니다.");
            }
            
            // 비밀번호 확인
            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                log.info("User {} authenticated successfully from DB", username);
                return new AuthResult(generateToken(user), user.getId(), user.getUsername(), user.getRole());
            } else {
                log.warn("Invalid password for user: {}", username);
                throw new IllegalArgumentException("Invalid credentials");
            }
        } catch (IllegalArgumentException e) {
            // 인증 실패는 그대로 전달
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during authentication for user {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("인증 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    public String generateToken(User user) {
        return generateToken(user.getUsername(), user.getRole(), user.getId());
    }
    
    public String generateToken(String username) {
        return generateToken(username, "ADMIN", null);
    }
    
    public String generateToken(String username, String role, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("role", role);
        if (userId != null) {
            claims.put("userId", userId);
        }

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000)) // 24시간
                .signWith(secretKey)
                .compact();
    }
    
    public static class AuthResult {
        private final String token;
        private final Long userId;
        private final String username;
        private final String role;
        
        public AuthResult(String token, Long userId, String username, String role) {
            this.token = token;
            this.userId = userId;
            this.username = username;
            this.role = role;
        }
        
        public String getToken() { return token; }
        public Long getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getRole() { return role; }
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

