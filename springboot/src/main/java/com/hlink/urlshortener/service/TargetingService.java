package com.hlink.urlshortener.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hlink.urlshortener.util.UserAgentParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TargetingService {

    private final UserAgentParser userAgentParser;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 타겟팅 규칙에 따라 리디렉션 URL 결정
     * @param defaultUrl 기본 URL
     * @param settings 타겟팅 설정 (JSON 문자열)
     * @param request HTTP 요청
     * @return 타겟팅된 URL
     */
    public String getTargetedUrl(String defaultUrl, String settings, HttpServletRequest request) {
        if (settings == null || settings.isEmpty()) {
            return defaultUrl;
        }

        try {
            Map<String, Object> settingsMap = parseSettings(settings);
            String userAgent = request.getHeader("User-Agent");
            String acceptLanguage = request.getHeader("Accept-Language");
            String language = parseLanguage(acceptLanguage);

            // 1. 기기별 타겟팅
            String deviceUrl = getDeviceTargetedUrl(settingsMap, userAgent);
            if (deviceUrl != null) {
                return deviceUrl;
            }

            // 2. 언어별 타겟팅
            String languageUrl = getLanguageTargetedUrl(settingsMap, language);
            if (languageUrl != null) {
                return languageUrl;
            }

            // 3. 모바일 딥링킹
            String deeplinkUrl = getDeeplinkUrl(settingsMap, userAgent);
            if (deeplinkUrl != null) {
                return deeplinkUrl;
            }

            return defaultUrl;
        } catch (Exception e) {
            log.error("Error in targeting: {}", e.getMessage());
            return defaultUrl;
        }
    }

    private String getDeviceTargetedUrl(Map<String, Object> settings, String userAgent) {
        try {
            Map<String, String> deviceRedirects = (Map<String, String>) settings.get("device_redirects");
            if (deviceRedirects == null) {
                return null;
            }

            if (userAgentParser.isMobile(userAgent)) {
                String mobileUrl = deviceRedirects.get("mobile");
                if (mobileUrl != null && !mobileUrl.isEmpty()) {
                    return mobileUrl;
                }
            }

            if (userAgentParser.isIOS(userAgent)) {
                String iosUrl = deviceRedirects.get("ios");
                if (iosUrl != null && !iosUrl.isEmpty()) {
                    return iosUrl;
                }
            }

            if (userAgentParser.isAndroid(userAgent)) {
                String androidUrl = deviceRedirects.get("android");
                if (androidUrl != null && !androidUrl.isEmpty()) {
                    return androidUrl;
                }
            }

            String deviceType = userAgentParser.parseDeviceType(userAgent);
            if ("Tablet".equals(deviceType)) {
                String tabletUrl = deviceRedirects.get("tablet");
                if (tabletUrl != null && !tabletUrl.isEmpty()) {
                    return tabletUrl;
                }
            }

            if ("Desktop".equals(deviceType)) {
                String desktopUrl = deviceRedirects.get("desktop");
                if (desktopUrl != null && !desktopUrl.isEmpty()) {
                    return desktopUrl;
                }
            }

            return null;
        } catch (Exception e) {
            log.error("Error parsing device redirects: {}", e.getMessage());
            return null;
        }
    }

    private String getLanguageTargetedUrl(Map<String, Object> settings, String language) {
        try {
            Map<String, String> languageRedirects = (Map<String, String>) settings.get("language_redirects");
            if (languageRedirects == null || language == null || language.equals("Unknown")) {
                return null;
            }

            return languageRedirects.get(language.toLowerCase());
        } catch (Exception e) {
            log.error("Error parsing language redirects: {}", e.getMessage());
            return null;
        }
    }

    private String getDeeplinkUrl(Map<String, Object> settings, String userAgent) {
        try {
            if (!userAgentParser.isMobile(userAgent)) {
                return null;
            }

            String deeplink = (String) settings.get("mobile_deeplink");
            if (deeplink != null && !deeplink.isEmpty()) {
                // 앱이 설치되어 있으면 딥링크, 없으면 스토어로 리디렉션
                return deeplink;
            }

            return null;
        } catch (Exception e) {
            log.error("Error parsing deeplink: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, Object> parseSettings(String settings) {
        try {
            if (settings.startsWith("{") || settings.startsWith("[")) {
                return objectMapper.readValue(settings, new TypeReference<Map<String, Object>>() {});
            }
            return new HashMap<>();
        } catch (Exception e) {
            log.error("Error parsing settings JSON: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    private String parseLanguage(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isEmpty()) {
            return "Unknown";
        }
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
}

