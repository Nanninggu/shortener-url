package com.hlink.urlshortener.controller;

import com.hlink.urlshortener.dto.ApiKeyCreateRequest;
import com.hlink.urlshortener.model.ApiKey;
import com.hlink.urlshortener.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/api-keys")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://223.130.157.227:3000", "http://223.130.157.227"})
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiKey> createApiKey(@Valid @RequestBody ApiKeyCreateRequest request) {
        ApiKey apiKey = apiKeyService.createApiKey(
            request.getKeyName(), 
            request.getUserId(), 
            request.getTeamId(), 
            request.getPermissions(), 
            request.getRateLimit()
        );
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

    @PostMapping("/{id}/activate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> activateApiKey(@PathVariable Long id) {
        apiKeyService.activateApiKey(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteApiKey(@PathVariable Long id) {
        apiKeyService.deleteApiKey(id);
        return ResponseEntity.ok().build();
    }
}

