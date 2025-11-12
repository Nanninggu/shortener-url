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
        log.info("Redirecting from {} to {}", shortCode, originalUrl);
        
        // Accept 헤더 확인하여 HTML을 요청하는 브라우저인지 체크
        String acceptHeader = request.getHeader("Accept");
        boolean wantsHtml = acceptHeader != null && acceptHeader.contains("text/html");
        
        // 브라우저가 HTML을 요청하는 경우 HTML 폴백 제공 (리다이렉트 실패 시 대비)
        // 하지만 QR 스캐너 앱 호환성을 위해 HTTP 302 상태 코드 사용
        if (wantsHtml) {
            String html = String.format(
                "<!DOCTYPE html><html><head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta http-equiv=\"refresh\" content=\"0; url=%s\">" +
                "<script>window.location.href='%s';</script>" +
                "</head><body><p>리다이렉트 중... <a href=\"%s\">여기를 클릭하세요</a></p></body></html>",
                originalUrl, originalUrl, originalUrl
            );
            // HTTP 302 사용 (QR 스캐너 앱 호환성)
            return ResponseEntity.status(HttpStatus.FOUND)
                    .contentType(MediaType.TEXT_HTML)
                    .header("Location", originalUrl)
                    .body(html);
        }
        
        // 모든 클라이언트에 대해 HTTP 302 리다이렉트 사용
        // QR 스캐너 앱, 모바일 브라우저, 데스크톱 브라우저 모두 지원
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", originalUrl)
                .build();
    }
}

