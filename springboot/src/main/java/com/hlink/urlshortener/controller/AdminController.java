package com.hlink.urlshortener.controller;

import com.hlink.urlshortener.dto.AdminStatsResponse;
import com.hlink.urlshortener.dto.H2DatabaseInfo;
import com.hlink.urlshortener.dto.UrlResponse;
import com.hlink.urlshortener.model.ApiKey;
import com.hlink.urlshortener.model.SupportTicket;
import com.hlink.urlshortener.model.Team;
import com.hlink.urlshortener.model.WhiteLabelSettings;
import com.hlink.urlshortener.service.ApiKeyService;
import com.hlink.urlshortener.service.SupportTicketService;
import com.hlink.urlshortener.service.TeamService;
import com.hlink.urlshortener.service.UrlService;
import com.hlink.urlshortener.service.UserService;
import com.hlink.urlshortener.service.WhiteLabelService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "관리자", description = "관리자 전용 API (인증 필요)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://223.130.157.227:3000", "http://223.130.157.227", "http://49.50.138.63", "https://49.50.138.63"})
public class AdminController {

    private final UrlService urlService;
    private final UserService userService;
    private final TeamService teamService;
    private final SupportTicketService supportTicketService;
    private final ApiKeyService apiKeyService;
    private final WhiteLabelService whiteLabelService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        AdminStatsResponse stats = urlService.getAdminStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/urls")
    public ResponseEntity<?> getAllUrls(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "all") String status) {
        // 검색어나 필터가 있으면 필터링된 페이징 결과 반환
        if ((search != null && !search.trim().isEmpty()) || !"all".equals(status)) {
            com.hlink.urlshortener.dto.PageResponse<UrlResponse> pageResponse = 
                urlService.getAllUrlsWithPagingAndFilter(page, size, search, status);
            return ResponseEntity.ok(pageResponse);
        }
        // 페이징 파라미터가 있으면 페이징된 결과 반환
        if (page >= 0 && size > 0) {
            com.hlink.urlshortener.dto.PageResponse<UrlResponse> pageResponse = urlService.getAllUrlsWithPaging(page, size);
            return ResponseEntity.ok(pageResponse);
        }
        // 페이징 파라미터가 없으면 전체 목록 반환 (하위 호환성)
        List<UrlResponse> urls = urlService.getAllUrls();
        return ResponseEntity.ok(urls);
    }

    @GetMapping("/database/info")
    public ResponseEntity<H2DatabaseInfo> getDatabaseInfo(HttpServletRequest request) {
        // 현재 요청의 호스트 정보 가져오기
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String host = scheme + "://" + serverName + (serverPort != 80 && serverPort != 443 ? ":" + serverPort : "");
        
        H2DatabaseInfo info = H2DatabaseInfo.builder()
                .databaseType("H2 Database")
                .jdbcUrl("jdbc:h2:tcp://localhost:9092/./data/url_shortener")
                .consoleUrl(host + "/h2-console")
                .tcpUrl("tcp://" + serverName + ":9092/./data/url_shortener")
                .username("sa")
                .password("")
                .tcpPort(9092)
                .databasePath("./data/url_shortener")
                .consoleEnabled(true)
                .externalAccessEnabled(true)
                .build();
        
        return ResponseEntity.ok(info);
    }

    @DeleteMapping("/urls/{id}")
    public ResponseEntity<Void> deleteUrl(@PathVariable Long id) {
        urlService.deleteUrl(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/urls/expired")
    public ResponseEntity<Void> deleteExpiredUrls() {
        urlService.deleteExpiredUrls();
        return ResponseEntity.noContent().build();
    }

    // ========== 사용자 관리 ==========
    
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<Map<String, String>> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String role = request.get("role");
        if (role == null || role.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "역할(role)은 필수입니다."));
        }
        userService.updateUserRole(id, role);
        return ResponseEntity.ok(Map.of("message", "사용자 역할이 변경되었습니다."));
    }

    @PutMapping("/users/{id}/plan")
    public ResponseEntity<Map<String, String>> updateUserPlan(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String planType = request.get("planType");
        if (planType == null || planType.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "플랜 타입(planType)은 필수입니다."));
        }
        userService.updateUserPlanType(id, planType);
        return ResponseEntity.ok(Map.of("message", "사용자 플랜이 변경되었습니다."));
    }

    // ========== 팀 관리 ==========
    
    @GetMapping("/teams")
    public ResponseEntity<List<Team>> getAllTeams() {
        List<Team> teams = teamService.findAll();
        return ResponseEntity.ok(teams);
    }

    @DeleteMapping("/teams/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    // ========== 지원 티켓 관리 ==========
    
    @GetMapping("/tickets")
    public ResponseEntity<List<SupportTicket>> getAllTickets() {
        List<SupportTicket> tickets = supportTicketService.findAll();
        return ResponseEntity.ok(tickets);
    }

    @DeleteMapping("/tickets/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        supportTicketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    // ========== API 키 관리 ==========
    
    @GetMapping("/api-keys")
    public ResponseEntity<List<ApiKey>> getAllApiKeys() {
        List<ApiKey> apiKeys = apiKeyService.findAll();
        return ResponseEntity.ok(apiKeys);
    }

    @DeleteMapping("/api-keys/{id}")
    public ResponseEntity<Void> deleteApiKey(@PathVariable Long id) {
        apiKeyService.deleteApiKey(id);
        return ResponseEntity.noContent().build();
    }

    // ========== 화이트라벨 설정 관리 ==========
    
    @GetMapping("/white-labels")
    public ResponseEntity<List<WhiteLabelSettings>> getAllWhiteLabels() {
        List<WhiteLabelSettings> settings = whiteLabelService.findAll();
        return ResponseEntity.ok(settings);
    }

    @DeleteMapping("/white-labels/{id}")
    public ResponseEntity<Void> deleteWhiteLabel(@PathVariable Long id) {
        whiteLabelService.deleteSettingsById(id);
        return ResponseEntity.noContent().build();
    }
}

