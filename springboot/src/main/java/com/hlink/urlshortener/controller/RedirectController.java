package com.hlink.urlshortener.controller;

import com.hlink.urlshortener.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.springframework.web.util.HtmlUtils.htmlEscape;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final UrlService urlService;

    @GetMapping("/{shortCode}")
    public ResponseEntity<?> redirect(@PathVariable String shortCode, HttpServletRequest request) {
        log.info("Redirect request received - shortCode: {}, IP: {}, User-Agent: {}", 
                shortCode, request.getRemoteAddr(), request.getHeader("User-Agent"));
        
        Optional<String> originalUrlOpt = urlService.getOriginalUrl(shortCode, request);
        
        if (originalUrlOpt.isEmpty()) {
            log.warn("Short code not found or expired: {}", shortCode);
            return ResponseEntity.notFound().build();
        }
        
        String originalUrl = originalUrlOpt.get();
        String userAgent = request.getHeader("User-Agent");
        log.info("Redirecting from {} to {} (User-Agent: {})", shortCode, originalUrl, userAgent);
        
        // URL 이스케이프 (XSS 방지 및 안전한 리디렉션)
        String escapedUrl = escapeHtml(originalUrl);
        
        // 모든 요청에 대해 HTML 폴백 제공 (모바일 브라우저 및 QR 스캐너 앱 호환성)
        // HTTP 302와 HTML 폴백을 모두 제공하여 최대 호환성 확보
            String html = String.format(
                "<!DOCTYPE html><html><head>" +
                "<meta charset=\"UTF-8\">" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<meta http-equiv=\"refresh\" content=\"0; url=%s\">" +
            "<title>리다이렉트 중...</title>" +
            "<script>" +
            "  // 즉시 리디렉션 시도 (모바일 브라우저 호환)" +
            "  (function() {" +
            "    try { window.location.replace('%s'); }" +
            "    catch(e) { window.location.href = '%s'; }" +
            "  })();" +
            "</script>" +
            "</head><body style=\"font-family: Arial, sans-serif; text-align: center; padding: 50px;\">" +
            "<p>리다이렉트 중...</p>" +
            "<p><a href=\"%s\" style=\"color: #0066cc; text-decoration: none;\">여기를 클릭하세요</a></p>" +
            "</body></html>",
            escapedUrl, escapedUrl, escapedUrl, escapedUrl
        );
        
        // HTTP 302와 HTML 폴백을 모두 제공
        // Location 헤더: QR 스캐너 앱 및 브라우저가 자동으로 리디렉션
        // HTML 본문: Location 헤더를 지원하지 않는 클라이언트를 위한 폴백
        return ResponseEntity.status(HttpStatus.FOUND)
                .contentType(MediaType.TEXT_HTML)
                .header("Location", originalUrl)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(html);
    }
    
    /**
     * HTML 이스케이프 (XSS 방지)
     */
    private String escapeHtml(String url) {
        if (url == null) {
            return "";
        }
        // URL은 일반적으로 HTML 특수문자가 포함되어 있지 않지만, 안전을 위해 이스케이프
        return htmlEscape(url);
    }
}

