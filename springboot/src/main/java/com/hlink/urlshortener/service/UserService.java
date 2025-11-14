package com.hlink.urlshortener.service;

import com.hlink.urlshortener.dto.UserCreateRequest;
import com.hlink.urlshortener.mapper.UserMapper;
import com.hlink.urlshortener.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(UserCreateRequest request) {
        // 중복 체크
        if (userMapper.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 사용자명입니다.");
        }
        if (userMapper.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        String planType = request.getPlanType() != null ? request.getPlanType() : "FREE";
        
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .planType(planType)
                .enabled(true) // 기본적으로 활성화된 상태로 생성
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userMapper.insert(user);
        log.info("User created: {}", user.getUsername());
        return user;
    }

    public Optional<User> findById(Long id) {
        return userMapper.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        try {
            return userMapper.findByUsername(username);
        } catch (Exception e) {
            log.error("Error finding user by username {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("사용자 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    public Optional<User> findByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    public List<User> findAll() {
        try {
            List<User> users = userMapper.findAll();
            log.debug("Found {} users", users != null ? users.size() : 0);
            return users != null ? users : List.of();
        } catch (Exception e) {
            log.error("Error finding all users: {}", e.getMessage(), e);
            throw new RuntimeException("사용자 목록을 조회하는 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void updateUser(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }

    @Transactional
    public void toggleUserStatus(Long id) {
        Optional<User> userOpt = userMapper.findById(id);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        userMapper.toggleEnabled(id);
        log.info("User status toggled for user ID: {}", id);
    }

    @Transactional
    public void updateUserRole(Long id, String role) {
        Optional<User> userOpt = userMapper.findById(id);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        userMapper.updateRole(id, role);
        log.info("User role updated for user ID: {} to role: {}", id, role);
    }

    @Transactional
    public void updateUserPlanType(Long id, String planType) {
        Optional<User> userOpt = userMapper.findById(id);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        userMapper.updatePlanType(id, planType);
        log.info("User plan type updated for user ID: {} to plan: {}", id, planType);
    }
}

