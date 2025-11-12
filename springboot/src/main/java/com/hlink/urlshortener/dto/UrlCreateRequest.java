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
}

