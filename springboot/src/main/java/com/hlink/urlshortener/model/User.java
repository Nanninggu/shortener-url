package com.hlink.urlshortener.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String email;
    private String passwordHash;
    private String role; // USER, ADMIN, PREMIUM
    private String planType; // FREE, BASIC, PRO, ENTERPRISE
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

