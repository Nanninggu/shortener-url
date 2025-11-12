package com.hlink.urlshortener.controller;

import com.hlink.urlshortener.dto.LoginRequest;
import com.hlink.urlshortener.dto.LoginResponse;
import com.hlink.urlshortener.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("Login attempt for user: {}", request.getUsername());
            String token = authService.authenticate(request.getUsername(), request.getPassword());
            
            LoginResponse response = LoginResponse.builder()
                    .token(token)
                    .username(request.getUsername())
                    .role("ADMIN")
                    .build();
            
            log.info("Login successful for user: {}", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Login failed for user: {} - {}", request.getUsername(), e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            log.error("Unexpected error during login for user: {}", request.getUsername(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "An error occurred during login");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

