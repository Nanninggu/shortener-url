package com.hlink.urlshortener.mapper;

import com.hlink.urlshortener.model.WhiteLabelSettings;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface WhiteLabelSettingsMapper {
    void insert(WhiteLabelSettings settings);
    Optional<WhiteLabelSettings> findByUserId(Long userId);
    Optional<WhiteLabelSettings> findByTeamId(Long teamId);
    Optional<WhiteLabelSettings> findByDomain(String domain);
    Optional<WhiteLabelSettings> findByTeamDomain(String domain);
    List<WhiteLabelSettings> findAll(); // 모든 화이트라벨 설정 조회 (관리자용)
    void update(WhiteLabelSettings settings);
    void deleteByUserId(Long userId);
    void deleteById(Long id); // ID로 삭제 (관리자용)
}

