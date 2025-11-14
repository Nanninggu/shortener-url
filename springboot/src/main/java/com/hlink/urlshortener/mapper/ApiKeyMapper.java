package com.hlink.urlshortener.mapper;

import com.hlink.urlshortener.model.ApiKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ApiKeyMapper {
    void insert(ApiKey apiKey);
    Optional<ApiKey> findById(Long id);
    Optional<ApiKey> findByApiKey(String apiKey);
    List<ApiKey> findByUserId(Long userId);
    List<ApiKey> findByTeamId(Long teamId);
    List<ApiKey> findAll(); // 모든 API 키 조회 (관리자용)
    void update(ApiKey apiKey);
    void updateActiveStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);
    void deleteById(Long id);
}

