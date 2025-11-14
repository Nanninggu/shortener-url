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
public class TicketComment {
    private Long id;
    private Long ticketId;
    private Long userId;
    private String comment;
    private LocalDateTime createdAt;
}

