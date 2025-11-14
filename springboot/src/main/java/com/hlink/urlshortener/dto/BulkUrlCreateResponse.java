package com.hlink.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkUrlCreateResponse {
    private int total;
    private int success;
    private int failed;
    private List<UrlResponse> successfulUrls;
    private List<BulkUrlError> errors;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkUrlError {
        private int index;
        private String originalUrl;
        private String error;
    }
}

