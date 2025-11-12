package com.hlink.urlshortener.controller;

import com.hlink.urlshortener.dto.AdminStatsResponse;
import com.hlink.urlshortener.dto.UrlResponse;
import com.hlink.urlshortener.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {

    private final UrlService urlService;

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        AdminStatsResponse stats = urlService.getAdminStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/urls")
    public ResponseEntity<List<UrlResponse>> getAllUrls() {
        List<UrlResponse> urls = urlService.getAllUrls();
        return ResponseEntity.ok(urls);
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
}

