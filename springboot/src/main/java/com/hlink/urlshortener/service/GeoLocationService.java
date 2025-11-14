package com.hlink.urlshortener.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GeoLocation 서비스
 * ip-api.com 무료 API를 사용하여 IP 기반 지역 정보를 조회합니다.
 * (무료 플랜: 분당 45회 요청 제한)
 */
@Service
@Slf4j
public class GeoLocationService {

    private static final String GEOIP_API_URL = "http://ip-api.com/json/";
    private static final int CACHE_SIZE = 1000;
    private static final long CACHE_TTL = 24 * 60 * 60 * 1000; // 24시간
    
    // IP 주소별 지역 정보 캐시 (IP -> {country, city, timestamp})
    private final Map<String, GeoInfo> geoCache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * IP 주소에서 국가 코드 추출
     * @param ipAddress IP 주소
     * @return 국가 코드 (ISO 3166-1 alpha-2), 알 수 없으면 null
     */
    public String getCountryCode(String ipAddress) {
        GeoInfo geoInfo = getGeoInfo(ipAddress);
        return geoInfo != null ? geoInfo.country : null;
    }

    /**
     * IP 주소에서 도시명 추출
     * @param ipAddress IP 주소
     * @return 도시명, 알 수 없으면 null
     */
    public String getCity(String ipAddress) {
        GeoInfo geoInfo = getGeoInfo(ipAddress);
        return geoInfo != null ? geoInfo.city : null;
    }

    /**
     * IP 주소에서 지역 정보 조회 (캐시 사용)
     */
    private GeoInfo getGeoInfo(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return null;
        }

        try {
            // 로컬호스트나 사설 IP 처리
            if (isPrivateIp(ipAddress)) {
                return null;
            }

            // 캐시 확인
            GeoInfo cached = geoCache.get(ipAddress);
            if (cached != null && (System.currentTimeMillis() - cached.timestamp) < CACHE_TTL) {
                return cached;
            }

            // API 호출
            GeoInfo geoInfo = fetchGeoInfoFromApi(ipAddress);
            if (geoInfo != null) {
                // 캐시에 저장 (캐시 크기 제한)
                if (geoCache.size() >= CACHE_SIZE) {
                    // 가장 오래된 항목 제거
                    geoCache.entrySet().removeIf(entry -> 
                        (System.currentTimeMillis() - entry.getValue().timestamp) >= CACHE_TTL);
                }
                geoCache.put(ipAddress, geoInfo);
            }
            
            return geoInfo;
        } catch (Exception e) {
            log.debug("Error getting geo info for IP {}: {}", ipAddress, e.getMessage());
            return null;
        }
    }

    /**
     * 사설 IP 주소 확인
     */
    private boolean isPrivateIp(String ipAddress) {
        return ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1") || 
                ipAddress.startsWith("192.168.") || ipAddress.startsWith("10.") || 
                ipAddress.startsWith("172.16.") || ipAddress.startsWith("172.17.") ||
                ipAddress.startsWith("172.18.") || ipAddress.startsWith("172.19.") ||
                ipAddress.startsWith("172.20.") || ipAddress.startsWith("172.21.") ||
                ipAddress.startsWith("172.22.") || ipAddress.startsWith("172.23.") ||
                ipAddress.startsWith("172.24.") || ipAddress.startsWith("172.25.") ||
                ipAddress.startsWith("172.26.") || ipAddress.startsWith("172.27.") ||
                ipAddress.startsWith("172.28.") || ipAddress.startsWith("172.29.") ||
               ipAddress.startsWith("172.30.") || ipAddress.startsWith("172.31.");
            }

    /**
     * ip-api.com API에서 지역 정보 조회
     */
    private GeoInfo fetchGeoInfoFromApi(String ipAddress) {
        try {
            URL url = new URL(GEOIP_API_URL + ipAddress);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000); // 2초 타임아웃
            conn.setReadTimeout(2000);
            conn.setRequestProperty("User-Agent", "H-Link-URL-Shortener/1.0");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                JsonNode json = objectMapper.readTree(conn.getInputStream());
                if (json.get("status").asText().equals("success")) {
                    String country = json.has("countryCode") ? json.get("countryCode").asText() : null;
                    String city = json.has("city") ? json.get("city").asText() : null;
                    return new GeoInfo(country, city, System.currentTimeMillis());
                }
            }
        } catch (Exception e) {
            log.debug("Failed to fetch geo info from API for IP {}: {}", ipAddress, e.getMessage());
        }
        return null;
    }

    /**
     * HTTP 요청에서 국가 코드 추출
     * @param request HTTP 요청
     * @return 국가 코드
     */
    public String getCountryCodeFromRequest(HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        return getCountryCode(ipAddress);
    }

    /**
     * HTTP 요청에서 도시명 추출
     * @param request HTTP 요청
     * @return 도시명
     */
    public String getCityFromRequest(HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        return getCity(ipAddress);
    }

    /**
     * 클라이언트 IP 주소 추출 (프록시 고려)
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // X-Forwarded-For는 여러 IP를 포함할 수 있음
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }

    /**
     * 지역 정보 캐시용 클래스
     */
    private static class GeoInfo {
        final String country;
        final String city;
        final long timestamp;

        GeoInfo(String country, String city, long timestamp) {
            this.country = country;
            this.city = city;
            this.timestamp = timestamp;
        }
    }
}

