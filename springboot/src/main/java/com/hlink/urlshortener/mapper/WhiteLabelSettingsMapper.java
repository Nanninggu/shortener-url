package com.hlink.urlshortener.mapper;

import com.hlink.urlshortener.model.WhiteLabelSettings;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface WhiteLabelSettingsMapper {
    void insert(WhiteLabelSettings settings);
    Optional<WhiteLabelSettings> findByUserId(Long userId);
    Optional<WhiteLabelSettings> findByTeamId(Long teamId);
    void update(WhiteLabelSettings settings);
    void deleteByUserId(Long userId);
}

