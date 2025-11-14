package com.hlink.urlshortener.controller;

import com.hlink.urlshortener.dto.WhiteLabelSettingsRequest;
import com.hlink.urlshortener.model.WhiteLabelSettings;
import com.hlink.urlshortener.service.WhiteLabelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/white-label")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://223.130.157.227:3000", "http://223.130.157.227", "http://49.50.138.63", "https://49.50.138.63"})
public class WhiteLabelController {

    private final WhiteLabelService whiteLabelService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WhiteLabelSettings> createOrUpdateSettings(
            @Valid @RequestBody WhiteLabelSettingsRequest request,
            @RequestParam Long userId,
            @RequestParam(required = false) Long teamId) {
        WhiteLabelSettings settings = whiteLabelService.createOrUpdateSettings(request, userId, teamId);
        return ResponseEntity.ok(settings);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<WhiteLabelSettings> getSettingsByUser(@PathVariable Long userId) {
        Optional<WhiteLabelSettings> settings = whiteLabelService.getSettingsByUserId(userId);
        return settings.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<WhiteLabelSettings> getSettingsByTeam(@PathVariable Long teamId) {
        Optional<WhiteLabelSettings> settings = whiteLabelService.getSettingsByTeamId(teamId);
        return settings.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

