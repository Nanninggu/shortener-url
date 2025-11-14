package com.hlink.urlshortener.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class BulkUrlCreateRequest {
    @NotEmpty(message = "URL 목록은 필수입니다")
    @Size(max = 100, message = "한 번에 최대 100개의 URL만 생성할 수 있습니다")
    @Valid
    private List<UrlCreateRequest> urls;
}

