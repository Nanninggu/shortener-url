package com.hlink.urlshortener.mapper;

import com.hlink.urlshortener.model.TeamMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface TeamMemberMapper {
    void insert(TeamMember teamMember);
    Optional<TeamMember> findByTeamIdAndUserId(@Param("teamId") Long teamId, @Param("userId") Long userId);
    List<TeamMember> findByTeamId(Long teamId);
    void deleteByTeamIdAndUserId(@Param("teamId") Long teamId, @Param("userId") Long userId);
    void deleteByTeamId(Long teamId);
}

