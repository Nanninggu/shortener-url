package com.hlink.urlshortener.controller;

import com.hlink.urlshortener.dto.AdminStatsResponse;
import com.hlink.urlshortener.dto.UrlResponse;
import com.hlink.urlshortener.model.SupportTicket;
import com.hlink.urlshortener.model.User;
import com.hlink.urlshortener.service.SupportTicketService;
import com.hlink.urlshortener.service.UrlService;
import com.hlink.urlshortener.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 전용 계정 관리자 컨트롤러
 * 특정 사용자나 팀의 계정을 관리하는 기능 제공
 */
@RestController
@RequestMapping("/api/account-manager")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://223.130.157.227:3000", "http://223.130.157.227", "http://49.50.138.63", "https://49.50.138.63"})
@PreAuthorize("hasRole('ADMIN')")
public class AccountManagerController {

    private final UserService userService;
    private final UrlService urlService;
    private final SupportTicketService supportTicketService;

    /**
     * 사용자 계정 정보 조회
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserAccount(@PathVariable Long userId) {
        return userService.findById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 사용자의 모든 URL 조회
     */
    @GetMapping("/users/{userId}/urls")
    public ResponseEntity<List<UrlResponse>> getUserUrls(@PathVariable Long userId) {
        List<UrlResponse> userUrls = urlService.getUrlsByUserId(userId);
        return ResponseEntity.ok(userUrls);
    }

    /**
     * 사용자의 통계 조회
     */
    @GetMapping("/users/{userId}/stats")
    public ResponseEntity<AdminStatsResponse> getUserStats(@PathVariable Long userId) {
        AdminStatsResponse stats = urlService.getUserStats(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * 사용자의 지원 티켓 조회
     */
    @GetMapping("/users/{userId}/tickets")
    public ResponseEntity<List<SupportTicket>> getUserTickets(@PathVariable Long userId) {
        List<SupportTicket> tickets = supportTicketService.findByUserId(userId);
        return ResponseEntity.ok(tickets);
    }

    /**
     * 사용자 계정 활성화/비활성화
     */
    @PostMapping("/users/{userId}/toggle-status")
    public ResponseEntity<Void> toggleUserStatus(@PathVariable Long userId) {
        try {
            userService.toggleUserStatus(userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 사용자 계정 삭제
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 사용자 역할 변경 (ADMIN ↔ USER)
     * Admin이 일반 계정에 admin 권한을 부여하거나, admin 계정에서 권한을 제거할 수 있습니다.
     */
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<Map<String, Object>> updateUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        try {
            String newRole = request.get("role");
            if (newRole == null || newRole.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "역할(role)은 필수입니다."));
            }
            
            // 역할 변경 실행
            userService.updateUserRole(userId, newRole);
            
            return ResponseEntity.ok(Map.of(
                "message", "사용자 역할이 변경되었습니다.",
                "userId", userId,
                "newRole", newRole
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "역할 변경 중 오류가 발생했습니다."));
        }
    }
}

