package com.hlink.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApiKeyCreateRequest {
    @NotBlank(message = "키 이름은 필수입니다")
    private String keyName;
    
    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;
    
    private Long teamId; // 선택사항
    
    private String permissions; // 선택사항 (JSON 문자열)
    
    private Integer rateLimit; // 선택사항
}

