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
public class WhiteLabelSettings {
    private Long id;
    private Long userId;
    private Long teamId;
    private String domain;
    private String brandName;
    private String logoUrl;
    private String primaryColor;
    private String secondaryColor;
    private String customCss;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

