package com.hlink.urlshortener.service;

import com.hlink.urlshortener.dto.AdminStatsResponse;
import com.hlink.urlshortener.dto.UrlCreateRequest;
import com.hlink.urlshortener.dto.UrlResponse;
import com.hlink.urlshortener.mapper.UrlClickMapper;
import com.hlink.urlshortener.mapper.UrlMapper;
import com.hlink.urlshortener.model.Url;
import com.hlink.urlshortener.model.UrlClick;
import com.hlink.urlshortener.util.UserAgentParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlMapper urlMapper;
    private final UrlClickMapper urlClickMapper;
    private final UrlValidationService urlValidationService;
    private final UserAgentParser userAgentParser;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Transactional
    public UrlResponse createShortUrl(UrlCreateRequest request) {
        try {
            log.info("Creating short URL for: {}", request.getOriginalUrl());
            
        // URL 검증
        if (!urlValidationService.isValidUrl(request.getOriginalUrl())) {
                log.warn("Invalid URL rejected: {}", request.getOriginalUrl());
            throw new IllegalArgumentException("유효하지 않거나 안전하지 않은 URL입니다.");
        }
        
        // 커스텀 단축 코드가 있는지 확인
        String shortCode = request.getCustomCode() != null && !request.getCustomCode().isEmpty()
                ? request.getCustomCode()
                : generateShortCode(request.getOriginalUrl());
        
            log.debug("Generated short code: {}", shortCode);
            
        // 커스텀 코드 중복 체크
        if (urlMapper.findByShortCode(shortCode).isPresent()) {
                log.warn("Short code already exists: {}", shortCode);
            throw new IllegalArgumentException("이미 사용 중인 단축 코드입니다.");
        }
        
        // 만료일 설정
        LocalDateTime expiresAt = null;
        if (request.getExpirationDays() != null && request.getExpirationDays() > 0) {
            expiresAt = LocalDateTime.now().plusDays(request.getExpirationDays());
        }

        Url url = Url.builder()
                .originalUrl(request.getOriginalUrl())
                .shortCode(shortCode)
                .clickCount(0L)
                .createdAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .build();

            log.debug("Inserting URL into database: {}", url);
        urlMapper.insert(url);
            log.debug("URL inserted successfully, ID: {}", url.getId());

            // ID가 설정되었는지 확인
            if (url.getId() == null) {
                log.error("URL ID is null after insert. This may indicate a database issue.");
                throw new RuntimeException("데이터베이스에 URL을 저장하는 중 오류가 발생했습니다.");
            }

            UrlResponse response = buildUrlResponse(url);
            log.info("Short URL created successfully: {} -> {}", shortCode, response.getShortUrl());
            return response;
            
        } catch (IllegalArgumentException e) {
            // 검증 오류는 그대로 전달
            throw e;
        } catch (Exception e) {
            log.error("Error creating short URL: {}", e.getMessage(), e);
            throw new RuntimeException("단축 URL 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Optional<String> getOriginalUrl(String shortCode, HttpServletRequest request) {
        log.debug("Getting original URL for shortCode: {}", shortCode);
        
        Optional<Url> urlOpt = urlMapper.findByShortCode(shortCode);
        
        if (urlOpt.isEmpty()) {
            log.warn("Short code not found in database: {}", shortCode);
            return Optional.empty();
        }

        Url url = urlOpt.get();
        log.debug("Found URL - ID: {}, Original: {}, ExpiresAt: {}", 
                url.getId(), url.getOriginalUrl(), url.getExpiresAt());
        
        // 만료 확인
        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Expired URL accessed: {} (expired at: {})", shortCode, url.getExpiresAt());
            return Optional.empty();
        }

        // 클릭 수 증가
        urlMapper.updateClickCount(url.getId());
        log.debug("Click count updated for URL ID: {}", url.getId());

        // 클릭 통계 기록
        try {
            recordClick(url.getId(), request);
            log.debug("Click recorded for URL ID: {}", url.getId());
        } catch (Exception e) {
            log.error("Failed to record click for URL ID: {}", url.getId(), e);
            // 클릭 기록 실패해도 리다이렉트는 진행
        }

        String originalUrl = url.getOriginalUrl();
        log.info("Returning original URL: {}", originalUrl);
        return Optional.of(originalUrl);
    }

    private void recordClick(Long urlId, HttpServletRequest request) {
        try {
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            String referer = request.getHeader("Referer");
            String acceptLanguage = request.getHeader("Accept-Language");
            String language = parseLanguage(acceptLanguage);
            
            UrlClick click = UrlClick.builder()
                    .urlId(urlId)
                    .clickedAt(LocalDateTime.now())
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .referer(referer)
                    .country("Unknown") // IP 기반 지역 정보는 외부 API 필요
                    .city("Unknown")
                    .deviceType(userAgentParser.parseDeviceType(userAgent))
                    .deviceBrand(userAgentParser.parseDeviceBrand(userAgent))
                    .os(userAgentParser.parseOS(userAgent))
                    .browser(userAgentParser.parseBrowser(userAgent))
                    .language(language)
                    .build();
            
            urlClickMapper.insert(click);
        } catch (Exception e) {
            log.error("Failed to record click: {}", e.getMessage());
        }
    }

    private String parseLanguage(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isEmpty()) {
            return "Unknown";
        }
        // Accept-Language: en-US,en;q=0.9 -> en
        String[] parts = acceptLanguage.split(",");
        if (parts.length > 0) {
            String lang = parts[0].split(";")[0].trim();
            if (lang.contains("-")) {
                return lang.split("-")[0];
            }
            return lang;
        }
        return "Unknown";
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    public Optional<UrlResponse> getUrlInfo(String shortCode) {
        return urlMapper.findByShortCode(shortCode)
                .map(this::buildUrlResponse);
    }

    public List<UrlResponse> getAllUrls() {
        try {
            List<Url> urls = urlMapper.findAll();
            if (urls == null || urls.isEmpty()) {
                return java.util.Collections.emptyList();
            }
            return urls.stream()
                .map(this::buildUrlResponse)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting all URLs: {}", e.getMessage(), e);
            return java.util.Collections.emptyList();
        }
    }

    @Transactional
    public void deleteUrl(Long id) {
        urlMapper.deleteById(id);
    }

    public AdminStatsResponse getAdminStats() {
        try {
        Long totalUrls = urlMapper.countAll();
        Long totalClicks = urlMapper.sumClickCount();
            
            // null 값 처리
            if (totalUrls == null) {
                totalUrls = 0L;
            }
            if (totalClicks == null) {
                totalClicks = 0L;
            }
        
        List<Url> allUrls = urlMapper.findAll();
        LocalDateTime now = LocalDateTime.now();
        
            long activeUrls = 0;
            long expiredUrls = 0;
            
            if (allUrls != null) {
                activeUrls = allUrls.stream()
                .filter(url -> url.getExpiresAt() == null || url.getExpiresAt().isAfter(now))
                .count();
        
                expiredUrls = allUrls.stream()
                .filter(url -> url.getExpiresAt() != null && url.getExpiresAt().isBefore(now))
                .count();
            }
        
        return AdminStatsResponse.builder()
                .totalUrls(totalUrls)
                .totalClicks(totalClicks)
                .activeUrls(activeUrls)
                .expiredUrls(expiredUrls)
                .build();
        } catch (Exception e) {
            log.error("Error getting admin stats: {}", e.getMessage(), e);
            // 에러 발생 시 기본값 반환
            return AdminStatsResponse.builder()
                    .totalUrls(0L)
                    .totalClicks(0L)
                    .activeUrls(0L)
                    .expiredUrls(0L)
                    .build();
        }
    }

    @Transactional
    public void deleteExpiredUrls() {
        urlMapper.deleteExpiredUrls();
    }

    private String generateShortCode(String originalUrl) {
        // 간단한 해시 기반 단축 코드 생성
        // 실제 운영 환경에서는 더 복잡한 알고리즘 사용 권장
        String hash = String.valueOf(originalUrl.hashCode());
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(hash.getBytes());
        
        // 8자리로 제한
        if (encoded.length() > 8) {
            encoded = encoded.substring(0, 8);
        }
        
        // 중복 체크 및 재생성
        int attempts = 0;
        while (urlMapper.findByShortCode(encoded).isPresent() && attempts < 10) {
            encoded = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString((originalUrl + System.currentTimeMillis()).getBytes())
                    .substring(0, 8);
            attempts++;
        }
        
        return encoded;
    }

    private UrlResponse buildUrlResponse(Url url) {
        try {
            if (url == null) {
                log.error("Cannot build response: URL is null");
                throw new IllegalArgumentException("URL cannot be null");
            }
            
        String shortUrl = buildShortUrl(url.getShortCode());
        
        return UrlResponse.builder()
                .id(url.getId())
                .originalUrl(url.getOriginalUrl())
                .shortUrl(shortUrl)
                .shortCode(url.getShortCode())
                    .clickCount(url.getClickCount() != null ? url.getClickCount() : 0L)
                .createdAt(url.getCreatedAt())
                .expiresAt(url.getExpiresAt())
                .build();
        } catch (Exception e) {
            log.error("Error building URL response: {}", e.getMessage(), e);
            throw new RuntimeException("URL 응답 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    private String buildShortUrl(String shortCode) {
        // 현재 요청 컨텍스트에서 Host 정보 가져오기
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String scheme = request.getScheme(); // http or https
                String serverName = request.getServerName(); // localhost or IP
                int serverPort = request.getServerPort();
                
                // 포트가 기본 포트(80, 443)가 아니면 포함
                String port = "";
                if ((scheme.equals("http") && serverPort != 80) || 
                    (scheme.equals("https") && serverPort != 443)) {
                    port = ":" + serverPort;
                }
                
                return scheme + "://" + serverName + port + "/" + shortCode;
            }
        } catch (Exception e) {
            log.debug("Could not get request context, using default base URL: {}", e.getMessage());
        }
        
        // 요청 컨텍스트가 없으면 기본 baseUrl 사용
        return baseUrl + "/" + shortCode;
    }
}

