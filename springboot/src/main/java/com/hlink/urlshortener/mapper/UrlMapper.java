package com.hlink.urlshortener.mapper;

import com.hlink.urlshortener.model.Url;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UrlMapper {
    void insert(Url url);
    
    Optional<Url> findByShortCode(@Param("shortCode") String shortCode);
    
    Optional<Url> findById(@Param("id") Long id);
    
    void updateClickCount(@Param("id") Long id);
    
    List<Url> findAll();
    
    void deleteExpiredUrls();
    
    void deleteById(@Param("id") Long id);
    
    Long countAll();
    
    Long sumClickCount();
}

