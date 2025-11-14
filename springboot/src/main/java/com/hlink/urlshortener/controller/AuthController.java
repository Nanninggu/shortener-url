package com.hlink.urlshortener.controller;

import com.hlink.urlshortener.dto.LoginRequest;
import com.hlink.urlshortener.dto.LoginResponse;
import com.hlink.urlshortener.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "인증", description = "사용자 인증 API (JWT 토큰 발급)")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:5173", "http://223.130.157.227:3000", "http://223.130.157.227"})
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "로그인", description = "사용자 인증 후 JWT 토큰을 발급받습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("Login attempt for user: {}", request.getUsername());
            AuthService.AuthResult authResult = authService.authenticate(request.getUsername(), request.getPassword());
            
            LoginResponse response = LoginResponse.builder()
                    .token(authResult.getToken())
                    .username(authResult.getUsername())
                    .role(authResult.getRole())
                    .userId(authResult.getUserId())
                    .build();
            
            log.info("Login successful for user: {} (userId: {})", request.getUsername(), authResult.getUserId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Login failed for user: {} - {}", request.getUsername(), e.getMessage());
            Map<String, String> error = new HashMap<>();
            String errorMessage = e.getMessage();
            if (errorMessage.contains("비활성화")) {
                error.put("message", errorMessage);
            } else {
                error.put("message", "사용자명 또는 비밀번호가 올바르지 않습니다.");
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            log.error("Unexpected error during login for user: {}", request.getUsername(), e);
            Map<String, String> error = new HashMap<>();
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("인증 중 오류")) {
                error.put("message", errorMessage);
            } else {
                error.put("message", "로그인 중 오류가 발생했습니다: " + (errorMessage != null ? errorMessage : e.getClass().getSimpleName()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

