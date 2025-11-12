package com.hlink.urlshortener.dto;

import lombok.Data;

@Data
public class WhiteLabelSettingsRequest {
    private String domain;
    private String brandName;
    private String logoUrl;
    private String primaryColor; // Hex color code
    private String secondaryColor; // Hex color code
    private String customCss;
}

