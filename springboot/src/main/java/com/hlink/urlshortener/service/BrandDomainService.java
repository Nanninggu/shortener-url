package com.hlink.urlshortener.service;

import com.hlink.urlshortener.mapper.WhiteLabelSettingsMapper;
import com.hlink.urlshortener.model.WhiteLabelSettings;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 브랜드 도메인 연결 서비스
 * 사용자나 팀의 브랜드 도메인을 확인하고 리디렉션에 사용
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BrandDomainService {

    private final WhiteLabelSettingsMapper whiteLabelSettingsMapper;

    /**
     * 요청의 Host 헤더가 브랜드 도메인인지 확인
     * @param request HTTP 요청
     * @return 브랜드 도메인 설정 (없으면 Optional.empty())
     */
    public Optional<WhiteLabelSettings> getBrandDomainFromRequest(HttpServletRequest request) {
        String host = request.getHeader("Host");
        if (host == null || host.isEmpty()) {
            host = request.getServerName();
        }
        
        // 포트 제거
        if (host.contains(":")) {
            host = host.split(":")[0];
        }
        
        log.debug("Checking brand domain for host: {}", host);
        
        // 사용자별 브랜드 도메인 확인
        Optional<WhiteLabelSettings> userSettings = whiteLabelSettingsMapper.findByDomain(host);
        if (userSettings.isPresent()) {
            log.debug("Found brand domain for user: {}", userSettings.get().getUserId());
            return userSettings;
        }
        
        // 팀별 브랜드 도메인 확인
        Optional<WhiteLabelSettings> teamSettings = whiteLabelSettingsMapper.findByTeamDomain(host);
        if (teamSettings.isPresent()) {
            log.debug("Found brand domain for team: {}", teamSettings.get().getTeamId());
            return teamSettings;
        }
        
        return Optional.empty();
    }

    /**
     * 브랜드 도메인을 사용하여 단축 URL 생성
     * @param shortCode 단축 코드
     * @param brandDomain 브랜드 도메인
     * @param request HTTP 요청
     * @return 브랜드 도메인을 포함한 단축 URL
     */
    public String buildBrandShortUrl(String shortCode, String brandDomain, HttpServletRequest request) {
        String scheme = request.getScheme();
        int port = request.getServerPort();
        
        String portStr = "";
        if ((scheme.equals("http") && port != 80) || (scheme.equals("https") && port != 443)) {
            portStr = ":" + port;
        }
        
        return scheme + "://" + brandDomain + portStr + "/" + shortCode;
    }
}

