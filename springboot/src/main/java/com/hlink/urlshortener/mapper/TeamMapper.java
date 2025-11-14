package com.hlink.urlshortener.mapper;

import com.hlink.urlshortener.model.Team;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface TeamMapper {
    void insert(Team team);
    Optional<Team> findById(Long id);
    List<Team> findByOwnerId(Long ownerId);
    List<Team> findByUserId(Long userId); // 사용자가 멤버로 속한 팀들
    List<Team> findAll(); // 모든 팀 조회 (관리자용)
    void update(Team team);
    void deleteById(Long id);
}

