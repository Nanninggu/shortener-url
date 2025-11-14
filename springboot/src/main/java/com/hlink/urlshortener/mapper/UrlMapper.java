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
    
    Optional<Url> findByOriginalUrl(@Param("originalUrl") String originalUrl);
    
    Optional<Url> findById(@Param("id") Long id);
    
    void updateClickCount(@Param("id") Long id);
    
    List<Url> findAll();
    
    List<Url> findAllWithPaging(@Param("offset") int offset, @Param("limit") int limit);
    
    List<Url> findAllWithPagingAndFilter(
            @Param("offset") int offset,
            @Param("limit") int limit,
            @Param("searchQuery") String searchQuery,
            @Param("statusFilter") String statusFilter);
    
    Long countAllWithFilter(
            @Param("searchQuery") String searchQuery,
            @Param("statusFilter") String statusFilter);
    
    List<Url> findByUserId(@Param("userId") Long userId);
    
    void deleteExpiredUrls();
    
    void deleteById(@Param("id") Long id);
    
    Long countAll();
    
    Long sumClickCount();
    
    Long countByUserId(@Param("userId") Long userId);
    
    Long sumClickCountByUserId(@Param("userId") Long userId);
}

