package com.hlink.urlshortener.controller;

import com.hlink.urlshortener.dto.UrlStatsResponse;
import com.hlink.urlshortener.mapper.UrlClickMapper;
import com.hlink.urlshortener.mapper.UrlMapper;
import com.hlink.urlshortener.model.Url;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag(name = "통계", description = "URL 클릭 통계 및 분석 API")
@Slf4j
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://223.130.157.227:3000", "http://223.130.157.227"})
public class StatsController {

    private final UrlClickMapper urlClickMapper;
    private final UrlMapper urlMapper;

    @Operation(summary = "URL 통계 조회", description = "단축 URL의 클릭 통계를 조회합니다. 시간별, 국가별, 디바이스별 등 다양한 통계를 제공합니다.")
    @GetMapping("/{shortCode}")
    public ResponseEntity<UrlStatsResponse> getUrlStats(
            @Parameter(description = "단축 코드", required = true) @PathVariable String shortCode,
            @Parameter(description = "조회 기간 (일)", example = "7") @RequestParam(defaultValue = "7") Integer days) {
        
        Optional<Url> urlOpt = urlMapper.findByShortCode(shortCode);
        if (urlOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Long urlId = urlOpt.get().getId();
        
        try {
            // 시작 날짜 계산
            java.time.LocalDateTime startDate = java.time.LocalDateTime.now().minusDays(days);
            log.debug("Fetching stats for URL ID: {}, startDate: {}", urlId, startDate);
            
            List<Map<String, Object>> clicksByHour = urlClickMapper.getClicksByHour(urlId, startDate);
            List<Map<String, Object>> clicksByCountry = urlClickMapper.getClicksByCountry(urlId);
            List<Map<String, Object>> clicksByDate = urlClickMapper.getClicksByDate(urlId, startDate);
            List<Map<String, Object>> clicksByDeviceType = urlClickMapper.getClicksByDeviceType(urlId);
            List<Map<String, Object>> clicksByBrowser = urlClickMapper.getClicksByBrowser(urlId);
            List<Map<String, Object>> clicksByOS = urlClickMapper.getClicksByOS(urlId);
            List<Map<String, Object>> clicksByReferer = urlClickMapper.getClicksByReferer(urlId);
            List<Map<String, Object>> clicksByCity = urlClickMapper.getClicksByCity(urlId);
            
            log.debug("Stats retrieved - Hour: {}, Country: {}, Date: {}, DeviceType: {}, Browser: {}, OS: {}, Referer: {}, City: {}", 
                    clicksByHour != null ? clicksByHour.size() : 0,
                    clicksByCountry != null ? clicksByCountry.size() : 0,
                    clicksByDate != null ? clicksByDate.size() : 0,
                    clicksByDeviceType != null ? clicksByDeviceType.size() : 0,
                    clicksByBrowser != null ? clicksByBrowser.size() : 0,
                    clicksByOS != null ? clicksByOS.size() : 0,
                    clicksByReferer != null ? clicksByReferer.size() : 0,
                    clicksByCity != null ? clicksByCity.size() : 0);

            UrlStatsResponse stats = UrlStatsResponse.builder()
                    .clicksByHour(clicksByHour != null ? clicksByHour : java.util.Collections.emptyList())
                    .clicksByCountry(clicksByCountry != null ? clicksByCountry : java.util.Collections.emptyList())
                    .clicksByDate(clicksByDate != null ? clicksByDate : java.util.Collections.emptyList())
                    .clicksByDeviceType(clicksByDeviceType != null ? clicksByDeviceType : java.util.Collections.emptyList())
                    .clicksByBrowser(clicksByBrowser != null ? clicksByBrowser : java.util.Collections.emptyList())
                    .clicksByOS(clicksByOS != null ? clicksByOS : java.util.Collections.emptyList())
                    .clicksByReferer(clicksByReferer != null ? clicksByReferer : java.util.Collections.emptyList())
                    .clicksByCity(clicksByCity != null ? clicksByCity : java.util.Collections.emptyList())
                    .build();

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching stats for URL ID {}: {}", urlId, e.getMessage(), e);
            // 데이터가 없거나 에러가 발생한 경우 빈 통계 반환
            UrlStatsResponse emptyStats = UrlStatsResponse.builder()
                    .clicksByHour(java.util.Collections.emptyList())
                    .clicksByCountry(java.util.Collections.emptyList())
                    .clicksByDate(java.util.Collections.emptyList())
                    .clicksByDeviceType(java.util.Collections.emptyList())
                    .clicksByBrowser(java.util.Collections.emptyList())
                    .clicksByOS(java.util.Collections.emptyList())
                    .clicksByReferer(java.util.Collections.emptyList())
                    .clicksByCity(java.util.Collections.emptyList())
                    .build();
            return ResponseEntity.ok(emptyStats);
        }
    }
}

