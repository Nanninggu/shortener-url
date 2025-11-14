package com.hlink.urlshortener.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hlink.urlshortener.dto.AdminStatsResponse;
import com.hlink.urlshortener.dto.BulkUrlCreateResponse;
import com.hlink.urlshortener.dto.UrlCreateRequest;
import com.hlink.urlshortener.dto.UrlResponse;
import com.hlink.urlshortener.mapper.UrlClickMapper;
import com.hlink.urlshortener.mapper.UrlMapper;
import com.hlink.urlshortener.mapper.UrlSettingsMapper;
import com.hlink.urlshortener.model.Url;
import com.hlink.urlshortener.model.UrlClick;
import com.hlink.urlshortener.model.UrlSettings;
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
import java.util.ArrayList;
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
    private final UrlSettingsMapper urlSettingsMapper;
    private final UrlValidationService urlValidationService;
    private final UserAgentParser userAgentParser;
    private final TargetingService targetingService;
    private final GeoLocationService geoLocationService;
    private final ObjectMapper objectMapper;

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
        
        // 커스텀 코드가 없는 경우, 같은 원본 URL이 이미 존재하는지 확인
        if (request.getCustomCode() == null || request.getCustomCode().isEmpty()) {
            Optional<Url> existingUrl = urlMapper.findByOriginalUrl(request.getOriginalUrl());
            if (existingUrl.isPresent()) {
                Url url = existingUrl.get();
                // 만료되지 않은 URL이면 기존 URL 반환
                if (url.getExpiresAt() == null || url.getExpiresAt().isAfter(LocalDateTime.now())) {
                    log.info("Existing URL found for: {}, returning existing short code: {}", 
                            request.getOriginalUrl(), url.getShortCode());
                    return buildUrlResponse(url);
                }
            }
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

            // 타겟팅 설정이 있으면 저장
            saveUrlSettings(url.getId(), request);

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
    public BulkUrlCreateResponse bulkCreateShortUrls(List<UrlCreateRequest> requests) {
        log.info("Bulk creating {} URLs", requests.size());
        
        List<UrlResponse> successfulUrls = new ArrayList<>();
        List<BulkUrlCreateResponse.BulkUrlError> errors = new ArrayList<>();
        
        for (int i = 0; i < requests.size(); i++) {
            UrlCreateRequest request = requests.get(i);
            try {
                UrlResponse response = createShortUrl(request);
                successfulUrls.add(response);
                log.debug("Successfully created URL {}: {}", i + 1, response.getShortUrl());
            } catch (IllegalArgumentException e) {
                // 검증 오류
                errors.add(BulkUrlCreateResponse.BulkUrlError.builder()
                        .index(i)
                        .originalUrl(request.getOriginalUrl())
                        .error(e.getMessage())
                        .build());
                log.warn("Failed to create URL {}: {}", i + 1, e.getMessage());
            } catch (Exception e) {
                // 기타 오류
                errors.add(BulkUrlCreateResponse.BulkUrlError.builder()
                        .index(i)
                        .originalUrl(request.getOriginalUrl())
                        .error("URL 생성 중 오류가 발생했습니다: " + e.getMessage())
                        .build());
                log.error("Error creating URL {}: {}", i + 1, e.getMessage());
            }
        }
        
        BulkUrlCreateResponse response = BulkUrlCreateResponse.builder()
                .total(requests.size())
                .success(successfulUrls.size())
                .failed(errors.size())
                .successfulUrls(successfulUrls)
                .errors(errors)
                .build();
        
        log.info("Bulk creation completed: {} successful, {} failed out of {}", 
                response.getSuccess(), response.getFailed(), response.getTotal());
        
        return response;
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
        
        // 타겟팅 설정이 있으면 타겟팅 서비스 사용
        String targetedUrl = originalUrl;
        try {
            Optional<UrlSettings> settingsOpt = urlSettingsMapper.findByUrlId(url.getId());
            if (settingsOpt.isPresent()) {
                UrlSettings settings = settingsOpt.get();
                String targetingSettingsJson = buildTargetingSettingsJson(settings);
                if (targetingSettingsJson != null && !targetingSettingsJson.isEmpty()) {
                    targetedUrl = targetingService.getTargetedUrl(originalUrl, targetingSettingsJson, request);
                    log.debug("Targeting applied: {} -> {}", originalUrl, targetedUrl);
                }
            }
        } catch (Exception e) {
            log.error("Error applying targeting for URL ID {}: {}", url.getId(), e.getMessage());
            // 타겟팅 실패 시 기본 URL 사용
        }
        
        log.info("Returning original URL: {} (targeted: {})", originalUrl, targetedUrl);
        return Optional.of(targetedUrl);
    }

    private void recordClick(Long urlId, HttpServletRequest request) {
        try {
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            String referer = request.getHeader("Referer");
            String acceptLanguage = request.getHeader("Accept-Language");
            String language = parseLanguage(acceptLanguage);
            
            // IP 기반 지역 정보 조회 (비동기로 처리하여 응답 속도에 영향 없도록)
            String country = null;
            String city = null;
            try {
                country = geoLocationService.getCountryCode(ipAddress);
                city = geoLocationService.getCity(ipAddress);
            } catch (Exception e) {
                log.debug("Failed to get geo location for IP {}: {}", ipAddress, e.getMessage());
                // 지역 정보 조회 실패해도 클릭 기록은 계속 진행
            }
            
            UrlClick click = UrlClick.builder()
                    .urlId(urlId)
                    .clickedAt(LocalDateTime.now())
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .referer(referer)
                    .country(country) // IP 기반 지역 정보 (VARCHAR(2)이므로 국가 코드만 저장)
                    .city(city) // 도시명
                    .deviceType(userAgentParser.parseDeviceType(userAgent))
                    .deviceBrand(userAgentParser.parseDeviceBrand(userAgent))
                    .os(userAgentParser.parseOS(userAgent))
                    .browser(userAgentParser.parseBrowser(userAgent))
                    .language(language)
                    .build();
            
            log.debug("Recording click for URL ID: {}, deviceType: {}, os: {}, browser: {}, country: {}, city: {}", 
                    urlId, click.getDeviceType(), click.getOs(), click.getBrowser(), country, city);
            urlClickMapper.insert(click);
            log.debug("Click recorded successfully for URL ID: {}", urlId);
        } catch (Exception e) {
            log.error("Failed to record click for URL ID {}: {}", urlId, e.getMessage(), e);
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
    
    public com.hlink.urlshortener.dto.PageResponse<UrlResponse> getAllUrlsWithPaging(int page, int size) {
        try {
            int offset = page * size;
            List<Url> urls = urlMapper.findAllWithPaging(offset, size);
            long totalElements = urlMapper.countAll();
            
            List<UrlResponse> urlResponses = urls.stream()
                .map(this::buildUrlResponse)
                .collect(Collectors.toList());
            
            return com.hlink.urlshortener.dto.PageResponse.of(urlResponses, page, size, totalElements);
        } catch (Exception e) {
            log.error("Error getting URLs with paging: {}", e.getMessage(), e);
            return com.hlink.urlshortener.dto.PageResponse.of(java.util.Collections.emptyList(), page, size, 0);
        }
    }
    
    public com.hlink.urlshortener.dto.PageResponse<UrlResponse> getAllUrlsWithPagingAndFilter(
            int page, int size, String searchQuery, String statusFilter) {
        try {
            int offset = page * size;
            List<Url> urls = urlMapper.findAllWithPagingAndFilter(offset, size, searchQuery, statusFilter);
            long totalElements = urlMapper.countAllWithFilter(searchQuery, statusFilter);
            
            List<UrlResponse> urlResponses = urls.stream()
                .map(this::buildUrlResponse)
                .collect(Collectors.toList());
            
            return com.hlink.urlshortener.dto.PageResponse.of(urlResponses, page, size, totalElements);
        } catch (Exception e) {
            log.error("Error getting URLs with paging and filter: {}", e.getMessage(), e);
            return com.hlink.urlshortener.dto.PageResponse.of(java.util.Collections.emptyList(), page, size, 0);
        }
    }

    public List<UrlResponse> getUrlsByUserId(Long userId) {
        try {
            List<Url> urls = urlMapper.findByUserId(userId);
            if (urls == null || urls.isEmpty()) {
                return java.util.Collections.emptyList();
            }
            return urls.stream()
                .map(this::buildUrlResponse)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting URLs for user {}: {}", userId, e.getMessage(), e);
            return java.util.Collections.emptyList();
        }
    }

    public AdminStatsResponse getUserStats(Long userId) {
        try {
            Long totalUrls = urlMapper.countByUserId(userId);
            Long totalClicks = urlMapper.sumClickCountByUserId(userId);
            
            // null 값 처리
            if (totalUrls == null) {
                totalUrls = 0L;
            }
            if (totalClicks == null) {
                totalClicks = 0L;
            }
        
            List<Url> userUrls = urlMapper.findByUserId(userId);
            LocalDateTime now = LocalDateTime.now();
        
            long activeUrls = 0;
            long expiredUrls = 0;
        
            if (userUrls != null) {
                activeUrls = userUrls.stream()
                    .filter(url -> url.getExpiresAt() == null || url.getExpiresAt().isAfter(now))
                    .count();
        
                expiredUrls = userUrls.stream()
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
            log.error("Error getting user stats for user {}: {}", userId, e.getMessage(), e);
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

    /**
     * URL 타겟팅 설정 저장
     */
    private void saveUrlSettings(Long urlId, UrlCreateRequest request) {
        // 타겟팅 설정이 하나라도 있으면 저장
        boolean hasSettings = request.getMobileDeeplink() != null || 
                             request.getDesktopUrl() != null ||
                             request.getTabletUrl() != null ||
                             request.getIosUrl() != null ||
                             request.getAndroidUrl() != null ||
                             request.getLanguageRedirects() != null ||
                             request.getRegionRedirects() != null ||
                             request.getDeviceRedirects() != null ||
                             (request.getDynamicQrEnabled() != null && request.getDynamicQrEnabled()) ||
                             request.getQrCustomData() != null;

        if (!hasSettings) {
            return; // 타겟팅 설정이 없으면 저장하지 않음
        }

        try {
            // device_redirects JSON 구성
            String deviceRedirectsJson = buildDeviceRedirectsJson(request);
            
            UrlSettings settings = UrlSettings.builder()
                    .urlId(urlId)
                    .mobileDeeplink(request.getMobileDeeplink())
                    .desktopUrl(request.getDesktopUrl())
                    .tabletUrl(request.getTabletUrl())
                    .iosUrl(request.getIosUrl())
                    .androidUrl(request.getAndroidUrl())
                    .languageRedirects(request.getLanguageRedirects())
                    .regionRedirects(request.getRegionRedirects())
                    .deviceRedirects(deviceRedirectsJson)
                    .dynamicQrEnabled(request.getDynamicQrEnabled() != null ? request.getDynamicQrEnabled() : false)
                    .qrCustomData(request.getQrCustomData())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            urlSettingsMapper.insert(settings);
            log.debug("URL settings saved for URL ID: {}", urlId);
        } catch (Exception e) {
            log.error("Error saving URL settings for URL ID {}: {}", urlId, e.getMessage());
            // 타겟팅 설정 저장 실패해도 URL 생성은 성공으로 처리
        }
    }

    /**
     * device_redirects JSON 문자열 구성
     */
    private String buildDeviceRedirectsJson(UrlCreateRequest request) {
        try {
            java.util.Map<String, String> deviceRedirects = new java.util.HashMap<>();
            
            if (request.getMobileDeeplink() != null && !request.getMobileDeeplink().isEmpty()) {
                // mobile_deeplink는 별도 필드로 저장되므로 device_redirects에는 포함하지 않음
            }
            if (request.getDesktopUrl() != null && !request.getDesktopUrl().isEmpty()) {
                deviceRedirects.put("desktop", request.getDesktopUrl());
            }
            if (request.getTabletUrl() != null && !request.getTabletUrl().isEmpty()) {
                deviceRedirects.put("tablet", request.getTabletUrl());
            }
            if (request.getIosUrl() != null && !request.getIosUrl().isEmpty()) {
                deviceRedirects.put("ios", request.getIosUrl());
            }
            if (request.getAndroidUrl() != null && !request.getAndroidUrl().isEmpty()) {
                deviceRedirects.put("android", request.getAndroidUrl());
            }
            
            // mobile은 mobileDeeplink가 있으면 포함
            if (request.getMobileDeeplink() != null && !request.getMobileDeeplink().isEmpty()) {
                deviceRedirects.put("mobile", request.getMobileDeeplink());
            }
            
            if (deviceRedirects.isEmpty()) {
                return request.getDeviceRedirects(); // 사용자가 직접 제공한 경우
            }
            
            return objectMapper.writeValueAsString(deviceRedirects);
        } catch (Exception e) {
            log.error("Error building device redirects JSON: {}", e.getMessage());
            return request.getDeviceRedirects(); // 실패 시 사용자 제공 값 반환
        }
    }

    /**
     * 타겟팅 설정을 JSON 문자열로 변환 (TargetingService에 전달)
     */
    private String buildTargetingSettingsJson(UrlSettings settings) {
        try {
            java.util.Map<String, Object> targetingMap = new java.util.HashMap<>();
            
            // 모바일 딥링크
            if (settings.getMobileDeeplink() != null && !settings.getMobileDeeplink().isEmpty()) {
                targetingMap.put("mobile_deeplink", settings.getMobileDeeplink());
            }
            
            // device_redirects
            if (settings.getDeviceRedirects() != null && !settings.getDeviceRedirects().isEmpty()) {
                try {
                    java.util.Map<String, String> deviceRedirects = objectMapper.readValue(
                        settings.getDeviceRedirects(), 
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, String>>() {}
                    );
                    targetingMap.put("device_redirects", deviceRedirects);
                } catch (Exception e) {
                    log.warn("Error parsing device_redirects JSON: {}", e.getMessage());
                }
            } else {
                // 개별 필드로부터 device_redirects 구성
                java.util.Map<String, String> deviceRedirects = new java.util.HashMap<>();
                if (settings.getDesktopUrl() != null && !settings.getDesktopUrl().isEmpty()) {
                    deviceRedirects.put("desktop", settings.getDesktopUrl());
                }
                if (settings.getTabletUrl() != null && !settings.getTabletUrl().isEmpty()) {
                    deviceRedirects.put("tablet", settings.getTabletUrl());
                }
                if (settings.getIosUrl() != null && !settings.getIosUrl().isEmpty()) {
                    deviceRedirects.put("ios", settings.getIosUrl());
                }
                if (settings.getAndroidUrl() != null && !settings.getAndroidUrl().isEmpty()) {
                    deviceRedirects.put("android", settings.getAndroidUrl());
                }
                if (settings.getMobileDeeplink() != null && !settings.getMobileDeeplink().isEmpty()) {
                    deviceRedirects.put("mobile", settings.getMobileDeeplink());
                }
                if (!deviceRedirects.isEmpty()) {
                    targetingMap.put("device_redirects", deviceRedirects);
                }
            }
            
            // language_redirects
            if (settings.getLanguageRedirects() != null && !settings.getLanguageRedirects().isEmpty()) {
                try {
                    java.util.Map<String, String> languageRedirects = objectMapper.readValue(
                        settings.getLanguageRedirects(),
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, String>>() {}
                    );
                    targetingMap.put("language_redirects", languageRedirects);
                } catch (Exception e) {
                    log.warn("Error parsing language_redirects JSON: {}", e.getMessage());
                }
            }
            
            // region_redirects
            if (settings.getRegionRedirects() != null && !settings.getRegionRedirects().isEmpty()) {
                try {
                    java.util.Map<String, String> regionRedirects = objectMapper.readValue(
                        settings.getRegionRedirects(),
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, String>>() {}
                    );
                    targetingMap.put("region_redirects", regionRedirects);
                } catch (Exception e) {
                    log.warn("Error parsing region_redirects JSON: {}", e.getMessage());
                }
            }
            
            if (targetingMap.isEmpty()) {
                return null;
            }
            
            return objectMapper.writeValueAsString(targetingMap);
        } catch (Exception e) {
            log.error("Error building targeting settings JSON: {}", e.getMessage());
            return null;
        }
    }
}

