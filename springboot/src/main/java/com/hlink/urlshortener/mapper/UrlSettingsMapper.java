package com.hlink.urlshortener.mapper;

import com.hlink.urlshortener.model.UrlSettings;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface UrlSettingsMapper {
    void insert(UrlSettings settings);
    
    Optional<UrlSettings> findByUrlId(@Param("urlId") Long urlId);
    
    void update(UrlSettings settings);
    
    void deleteByUrlId(@Param("urlId") Long urlId);
}

