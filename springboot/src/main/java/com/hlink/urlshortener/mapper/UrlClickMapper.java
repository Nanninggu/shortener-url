package com.hlink.urlshortener.mapper;

import com.hlink.urlshortener.model.UrlClick;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface UrlClickMapper {
    void insert(UrlClick urlClick);
    
    List<Map<String, Object>> getClicksByHour(@Param("urlId") Long urlId, @Param("startDate") LocalDateTime startDate);
    
    List<Map<String, Object>> getClicksByCountry(@Param("urlId") Long urlId);
    
    List<Map<String, Object>> getClicksByDate(@Param("urlId") Long urlId, @Param("startDate") LocalDateTime startDate);
    
    List<Map<String, Object>> getClicksByDeviceType(@Param("urlId") Long urlId);
    
    List<Map<String, Object>> getClicksByBrowser(@Param("urlId") Long urlId);
    
    List<Map<String, Object>> getClicksByOS(@Param("urlId") Long urlId);
    
    List<Map<String, Object>> getClicksByReferer(@Param("urlId") Long urlId);
    
    List<Map<String, Object>> getClicksByCity(@Param("urlId") Long urlId);
}

