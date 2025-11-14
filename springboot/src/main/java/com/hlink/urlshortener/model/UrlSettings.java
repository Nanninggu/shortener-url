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
public class UrlSettings {
    private Long id;
    private Long urlId;
    private String mobileDeeplink;
    private String desktopUrl;
    private String tabletUrl;
    private String iosUrl;
    private String androidUrl;
    private String languageRedirects; // JSON 문자열
    private String regionRedirects; // JSON 문자열
    private String deviceRedirects; // JSON 문자열
    private Boolean dynamicQrEnabled;
    private String qrCustomData; // JSON 문자열
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

