package com.hlink.urlshortener.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class UrlValidationService {

    private final WebClient webClient;
    
    // 악성 URL 패턴 (간단한 예시)
    private static final Set<String> MALICIOUS_PATTERNS = new HashSet<>(Arrays.asList(
        "phishing", "malware", "virus", "trojan", "spam"
    ));
    
    // 신뢰할 수 있는 도메인 화이트리스트 (선택사항)
    private static final Set<String> TRUSTED_DOMAINS = new HashSet<>(Arrays.asList(
        "google.com", "github.com", "stackoverflow.com"
    ));

    public UrlValidationService() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024))
                .build();
    }

    public boolean isValidUrl(String urlString) {
        try {
            URL url = new URI(urlString).toURL();
            
            // 기본 URL 형식 검증
            String protocol = url.getProtocol();
            if (!protocol.equals("http") && !protocol.equals("https")) {
                log.warn("Invalid protocol: {}", protocol);
                return false;
            }
            
            // 로컬호스트 차단 (보안)
            String host = url.getHost().toLowerCase();
            if (host.equals("localhost") || host.equals("127.0.0.1") || 
                host.startsWith("192.168.") || host.startsWith("10.") ||
                host.startsWith("172.16.") || host.startsWith("172.17.") ||
                host.startsWith("172.18.") || host.startsWith("172.19.") ||
                host.startsWith("172.20.") || host.startsWith("172.21.") ||
                host.startsWith("172.22.") || host.startsWith("172.23.") ||
                host.startsWith("172.24.") || host.startsWith("172.25.") ||
                host.startsWith("172.26.") || host.startsWith("172.27.") ||
                host.startsWith("172.28.") || host.startsWith("172.29.") ||
                host.startsWith("172.30.") || host.startsWith("172.31.")) {
                log.warn("Local/private network URL blocked: {}", host);
                return false;
            }
            
            // 악성 패턴 검사
            String urlLower = urlString.toLowerCase();
            for (String pattern : MALICIOUS_PATTERNS) {
                if (urlLower.contains(pattern)) {
                    log.warn("Malicious pattern detected: {}", pattern);
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            log.error("URL validation error: {}", e.getMessage());
            return false;
        }
    }

    public Mono<Boolean> checkUrlAccessibility(String urlString) {
        return webClient.get()
                .uri(urlString)
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode().is2xxSuccessful() || 
                                 response.getStatusCode().is3xxRedirection())
                .timeout(java.time.Duration.ofSeconds(5))
                .onErrorReturn(false)
                .doOnError(error -> {
                    if (error instanceof TimeoutException) {
                        log.warn("URL accessibility check timeout: {}", urlString);
                    } else {
                        log.warn("URL accessibility check failed: {}", urlString);
                    }
                });
    }
}

