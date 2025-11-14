package com.hlink.urlshortener.mapper;

import com.hlink.urlshortener.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {
    void insert(User user);
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    void update(User user);
    void updateRole(@Param("id") Long id, @Param("role") String role); // 역할 변경
    void updatePlanType(@Param("id") Long id, @Param("planType") String planType); // 플랜 타입 변경
    void deleteById(Long id);
    void toggleEnabled(@Param("id") Long id);
}

