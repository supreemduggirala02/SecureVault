package com.securevault.backend.controller;

import com.securevault.backend.entity.TeamMember;
import com.securevault.backend.service.TeamMemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/team")
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    public TeamMemberController(
            TeamMemberService teamMemberService) {

        this.teamMemberService = teamMemberService;
    }

    // =========================
    // ADD TEAM MEMBER
    // =========================

    @PostMapping("/members")
    public ResponseEntity<?> addMember(
            @RequestParam String memberEmail,
            @RequestParam String permission,
            Authentication authentication) {

        try {
            String ownerEmail = authentication.getName();

            TeamMember member =
                    teamMemberService.addMember(
                            ownerEmail,
                            memberEmail,
                            permission
                    );

            return ResponseEntity.ok(member);

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    // =========================
    // MY TEAM MEMBERS
    // =========================

    @GetMapping("/members")
    public ResponseEntity<?> getMyMembers(
            Authentication authentication) {

        try {
            String ownerEmail = authentication.getName();

            List<TeamMember> members =
                    teamMemberService.getMyMembers(
                            ownerEmail
                    );

            return ResponseEntity.ok(members);

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    // =========================
    // TEAMS WHERE I AM MEMBER
    // =========================

    @GetMapping("/my-teams")
    public ResponseEntity<?> getMyTeams(
            Authentication authentication) {

        try {
            String memberEmail = authentication.getName();

            List<TeamMember> teams =
                    teamMemberService.getMyTeams(
                            memberEmail
                    );

            return ResponseEntity.ok(teams);

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    // =========================
    // REMOVE TEAM MEMBER
    // =========================

    @DeleteMapping("/members/{id}")
    public ResponseEntity<?> removeMember(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            String ownerEmail = authentication.getName();

            teamMemberService.removeMember(
                    id,
                    ownerEmail
            );

            return ResponseEntity.ok(
                    "Team member removed successfully"
            );

        } catch (RuntimeException e) {

            if ("Access denied".equals(e.getMessage())) {

                return ResponseEntity.status(403)
                        .body(e.getMessage());
            }

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}