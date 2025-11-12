package com.hlink.urlshortener.mapper;

import com.hlink.urlshortener.model.ApiKey;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ApiKeyMapper {
    void insert(ApiKey apiKey);
    Optional<ApiKey> findById(Long id);
    Optional<ApiKey> findByApiKey(String apiKey);
    List<ApiKey> findByUserId(Long userId);
    List<ApiKey> findByTeamId(Long teamId);
    void update(ApiKey apiKey);
    void deleteById(Long id);
}

