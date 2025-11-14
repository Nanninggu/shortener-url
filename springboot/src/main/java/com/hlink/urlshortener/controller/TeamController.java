package com.hlink.urlshortener.controller;

import com.hlink.urlshortener.dto.TeamCreateRequest;
import com.hlink.urlshortener.model.Team;
import com.hlink.urlshortener.model.TeamMember;
import com.hlink.urlshortener.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://223.130.157.227:3000", "http://223.130.157.227"})
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Team> createTeam(@Valid @RequestBody TeamCreateRequest request,
                                          @RequestParam Long ownerId) {
        try {
            Team team = teamService.createTeam(request, ownerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(team);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Team> getTeam(@PathVariable Long id) {
        return teamService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Team>> getTeamsByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(teamService.findByOwnerId(ownerId));
    }

    @PostMapping("/{teamId}/members/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> addMember(@PathVariable Long teamId,
                                         @PathVariable Long userId,
                                         @RequestParam(defaultValue = "MEMBER") String role) {
        teamService.addMember(teamId, userId, role);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeMember(@PathVariable Long teamId, @PathVariable Long userId) {
        teamService.removeMember(teamId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{teamId}/members")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TeamMember>> getTeamMembers(@PathVariable Long teamId) {
        List<TeamMember> members = teamService.getTeamMembers(teamId);
        return ResponseEntity.ok(members);
    }
}

