package com.hlink.urlshortener.service;

import com.hlink.urlshortener.dto.TeamCreateRequest;
import com.hlink.urlshortener.mapper.TeamMapper;
import com.hlink.urlshortener.mapper.TeamMemberMapper;
import com.hlink.urlshortener.model.Team;
import com.hlink.urlshortener.model.TeamMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {

    private final TeamMapper teamMapper;
    private final TeamMemberMapper teamMemberMapper;

    @Transactional
    public Team createTeam(TeamCreateRequest request, Long ownerId) {
        Team team = Team.builder()
                .name(request.getName())
                .ownerId(ownerId)
                .createdAt(LocalDateTime.now())
                .build();

        teamMapper.insert(team);

        // 팀 소유자를 멤버로 추가
        TeamMember ownerMember = TeamMember.builder()
                .teamId(team.getId())
                .userId(ownerId)
                .role("OWNER")
                .createdAt(LocalDateTime.now())
                .build();
        teamMemberMapper.insert(ownerMember);

        log.info("Team created: {} by user {}", team.getName(), ownerId);
        return team;
    }

    public Optional<Team> findById(Long id) {
        return teamMapper.findById(id);
    }

    public List<Team> findByOwnerId(Long ownerId) {
        return teamMapper.findByOwnerId(ownerId);
    }

    public List<Team> findByUserId(Long userId) {
        return teamMapper.findByUserId(userId);
    }

    @Transactional
    public void addMember(Long teamId, Long userId, String role) {
        TeamMember member = TeamMember.builder()
                .teamId(teamId)
                .userId(userId)
                .role(role != null ? role : "MEMBER")
                .createdAt(LocalDateTime.now())
                .build();
        teamMemberMapper.insert(member);
    }

    @Transactional
    public void removeMember(Long teamId, Long userId) {
        teamMemberMapper.deleteByTeamIdAndUserId(teamId, userId);
    }

    @Transactional
    public void updateTeam(Team team) {
        teamMapper.update(team);
    }

    @Transactional
    public void deleteTeam(Long id) {
        teamMapper.deleteById(id);
    }
}

