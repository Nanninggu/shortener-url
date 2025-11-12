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
public class TeamMember {
    private Long id;
    private Long teamId;
    private Long userId;
    private String role; // OWNER, ADMIN, MEMBER
    private LocalDateTime createdAt;
}

