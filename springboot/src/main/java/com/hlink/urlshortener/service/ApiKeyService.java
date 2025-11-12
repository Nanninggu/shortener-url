package com.hlink.urlshortener.service;

import com.hlink.urlshortener.mapper.ApiKeyMapper;
import com.hlink.urlshortener.model.ApiKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiKeyService {

    private final ApiKeyMapper apiKeyMapper;
    private static final SecureRandom random = new SecureRandom();

    @Transactional
    public ApiKey createApiKey(String keyName, Long userId, Long teamId, String permissions, Integer rateLimit) {
        String apiKey = generateApiKey();
        
        ApiKey key = ApiKey.builder()
                .userId(userId)
                .teamId(teamId)
                .keyName(keyName)
                .apiKey(apiKey)
                .permissions(permissions != null ? permissions : "{}")
                .rateLimit(rateLimit != null ? rateLimit : 1000)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();

        apiKeyMapper.insert(key);
        log.info("API key created: {} for user {} / team {}", keyName, userId, teamId);
        return key;
    }

    public Optional<ApiKey> findById(Long id) {
        return apiKeyMapper.findById(id);
    }

    public Optional<ApiKey> findByApiKey(String apiKey) {
        return apiKeyMapper.findByApiKey(apiKey);
    }

    public List<ApiKey> findByUserId(Long userId) {
        return apiKeyMapper.findByUserId(userId);
    }

    public List<ApiKey> findByTeamId(Long teamId) {
        return apiKeyMapper.findByTeamId(teamId);
    }

    @Transactional
    public void updateApiKey(ApiKey apiKey) {
        apiKeyMapper.update(apiKey);
    }

    @Transactional
    public void deactivateApiKey(Long id) {
        Optional<ApiKey> keyOpt = apiKeyMapper.findById(id);
        if (keyOpt.isPresent()) {
            ApiKey key = keyOpt.get();
            key.setIsActive(false);
            apiKeyMapper.update(key);
        }
    }

    @Transactional
    public void deleteApiKey(Long id) {
        apiKeyMapper.deleteById(id);
    }

    public boolean validateApiKey(String apiKey) {
        Optional<ApiKey> keyOpt = apiKeyMapper.findByApiKey(apiKey);
        if (keyOpt.isEmpty()) {
            return false;
        }
        
        ApiKey key = keyOpt.get();
        
        // 만료 확인
        if (key.getExpiresAt() != null && key.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        return key.getIsActive();
    }

    private String generateApiKey() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return "hl_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

