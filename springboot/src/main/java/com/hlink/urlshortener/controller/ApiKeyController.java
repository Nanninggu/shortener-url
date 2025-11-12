package com.hlink.urlshortener.controller;

import com.hlink.urlshortener.model.ApiKey;
import com.hlink.urlshortener.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/api-keys")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiKey> createApiKey(@RequestParam String keyName,
                                               @RequestParam Long userId,
                                               @RequestParam(required = false) Long teamId,
                                               @RequestParam(required = false) String permissions,
                                               @RequestParam(required = false) Integer rateLimit) {
        ApiKey apiKey = apiKeyService.createApiKey(keyName, userId, teamId, permissions, rateLimit);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiKey);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ApiKey>> getApiKeysByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(apiKeyService.findByUserId(userId));
    }

    @GetMapping("/team/{teamId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ApiKey>> getApiKeysByTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(apiKeyService.findByTeamId(teamId));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deactivateApiKey(@PathVariable Long id) {
        apiKeyService.deactivateApiKey(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteApiKey(@PathVariable Long id) {
        apiKeyService.deleteApiKey(id);
        return ResponseEntity.ok().build();
    }
}

