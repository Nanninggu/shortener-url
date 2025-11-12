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
public class SupportTicket {
    private Long id;
    private Long userId;
    private String subject;
    private String description;
    private String priority; // LOW, NORMAL, HIGH, URGENT
    private String status; // OPEN, IN_PROGRESS, RESOLVED, CLOSED
    private Long assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

