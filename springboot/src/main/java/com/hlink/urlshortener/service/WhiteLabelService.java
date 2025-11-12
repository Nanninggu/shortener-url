package com.hlink.urlshortener.service;

import com.hlink.urlshortener.dto.WhiteLabelSettingsRequest;
import com.hlink.urlshortener.mapper.WhiteLabelSettingsMapper;
import com.hlink.urlshortener.model.WhiteLabelSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhiteLabelService {

    private final WhiteLabelSettingsMapper whiteLabelSettingsMapper;

    @Transactional
    public WhiteLabelSettings createOrUpdateSettings(WhiteLabelSettingsRequest request, Long userId, Long teamId) {
        Optional<WhiteLabelSettings> existingOpt = teamId != null 
                ? whiteLabelSettingsMapper.findByTeamId(teamId)
                : whiteLabelSettingsMapper.findByUserId(userId);

        WhiteLabelSettings settings;
        if (existingOpt.isPresent()) {
            settings = existingOpt.get();
            settings.setDomain(request.getDomain());
            settings.setBrandName(request.getBrandName());
            settings.setLogoUrl(request.getLogoUrl());
            settings.setPrimaryColor(request.getPrimaryColor());
            settings.setSecondaryColor(request.getSecondaryColor());
            settings.setCustomCss(request.getCustomCss());
            settings.setUpdatedAt(LocalDateTime.now());
            whiteLabelSettingsMapper.update(settings);
        } else {
            settings = WhiteLabelSettings.builder()
                    .userId(userId)
                    .teamId(teamId)
                    .domain(request.getDomain())
                    .brandName(request.getBrandName())
                    .logoUrl(request.getLogoUrl())
                    .primaryColor(request.getPrimaryColor())
                    .secondaryColor(request.getSecondaryColor())
                    .customCss(request.getCustomCss())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            whiteLabelSettingsMapper.insert(settings);
        }

        log.info("White label settings updated for user {} / team {}", userId, teamId);
        return settings;
    }

    public Optional<WhiteLabelSettings> getSettingsByUserId(Long userId) {
        return whiteLabelSettingsMapper.findByUserId(userId);
    }

    public Optional<WhiteLabelSettings> getSettingsByTeamId(Long teamId) {
        return whiteLabelSettingsMapper.findByTeamId(teamId);
    }

    @Transactional
    public void deleteSettings(Long userId) {
        whiteLabelSettingsMapper.deleteByUserId(userId);
    }
}

