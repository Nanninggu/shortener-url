package com.hlink.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UrlCreateRequest {
    @NotBlank(message = "URL은 필수입니다")
    @Pattern(regexp = "^(https?://).+", message = "유효한 HTTP 또는 HTTPS URL이어야 합니다")
    private String originalUrl;
    
    private Integer expirationDays; // 만료일 (선택사항)
    
    private String customCode; // 커스텀 단축 코드 (선택사항)
    
    // 타겟팅 설정 (JSON 문자열)
    private String mobileDeeplink; // 모바일 딥링크
    private String desktopUrl; // 데스크톱 URL
    private String tabletUrl; // 태블릿 URL
    private String iosUrl; // iOS URL
    private String androidUrl; // Android URL
    private String languageRedirects; // 언어별 리디렉션 (JSON)
    private String regionRedirects; // 지역별 리디렉션 (JSON)
    private String deviceRedirects; // 기기별 리디렉션 (JSON)
    private Boolean dynamicQrEnabled; // 동적 QR 코드 활성화
    private String qrCustomData; // QR 코드 커스텀 데이터 (JSON)
}

