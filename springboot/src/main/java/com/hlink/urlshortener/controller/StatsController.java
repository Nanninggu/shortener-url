package com.hlink.urlshortener.controller;

import com.hlink.urlshortener.dto.UrlStatsResponse;
import com.hlink.urlshortener.mapper.UrlClickMapper;
import com.hlink.urlshortener.mapper.UrlMapper;
import com.hlink.urlshortener.model.Url;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class StatsController {

    private final UrlClickMapper urlClickMapper;
    private final UrlMapper urlMapper;

    @GetMapping("/{shortCode}")
    public ResponseEntity<UrlStatsResponse> getUrlStats(
            @PathVariable String shortCode,
            @RequestParam(defaultValue = "7") Integer days) {
        
        Optional<Url> urlOpt = urlMapper.findByShortCode(shortCode);
        if (urlOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Long urlId = urlOpt.get().getId();
        
        try {
            // 시작 날짜 계산
            java.time.LocalDateTime startDate = java.time.LocalDateTime.now().minusDays(days);
            
            List<Map<String, Object>> clicksByHour = urlClickMapper.getClicksByHour(urlId, startDate);
            List<Map<String, Object>> clicksByCountry = urlClickMapper.getClicksByCountry(urlId);
            List<Map<String, Object>> clicksByDate = urlClickMapper.getClicksByDate(urlId, startDate);
            List<Map<String, Object>> clicksByDeviceType = urlClickMapper.getClicksByDeviceType(urlId);
            List<Map<String, Object>> clicksByBrowser = urlClickMapper.getClicksByBrowser(urlId);
            List<Map<String, Object>> clicksByOS = urlClickMapper.getClicksByOS(urlId);
            List<Map<String, Object>> clicksByReferer = urlClickMapper.getClicksByReferer(urlId);
            List<Map<String, Object>> clicksByCity = urlClickMapper.getClicksByCity(urlId);

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

