package com.securevault.backend.service;

import com.securevault.backend.entity.TeamMember;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.TeamMemberRepository;
import com.securevault.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    public TeamMemberService(
            TeamMemberRepository teamMemberRepository,
            UserRepository userRepository) {

        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
    }

    // Add team member
    public TeamMember addMember(
            String ownerEmail,
            String memberEmail,
            String permission) {

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new RuntimeException("Owner not found"));

        User member = userRepository.findByEmail(memberEmail)
                .orElseThrow(() ->
                        new RuntimeException("Member user not found"));

        if (owner.getId().equals(member.getId())) {
            throw new RuntimeException(
                    "You cannot add yourself as a team member");
        }

        if (teamMemberRepository
                .findByOwnerAndMember(owner, member)
                .isPresent()) {

            throw new RuntimeException(
                    "User is already a team member");
        }

        TeamMember teamMember = new TeamMember();

        teamMember.setOwner(owner);
        teamMember.setMember(member);
        teamMember.setPermission(permission);
        teamMember.setActive(true);

        return teamMemberRepository.save(teamMember);
    }

    // Get members added by owner
    public List<TeamMember> getMyMembers(
            String ownerEmail) {

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new RuntimeException("Owner not found"));

        return teamMemberRepository.findByOwner(owner);
    }

    // Get teams where current user is a member
    public List<TeamMember> getMyTeams(
            String memberEmail) {

        User member = userRepository.findByEmail(memberEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return teamMemberRepository.findByMember(member);
    }

    // Check whether user is an active team member
    public TeamMember getActiveMembership(
            String ownerEmail,
            String memberEmail) {

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new RuntimeException("Owner not found"));

        User member = userRepository.findByEmail(memberEmail)
                .orElseThrow(() ->
                        new RuntimeException("Member not found"));

        return teamMemberRepository
                .findByOwnerAndMemberAndActiveTrue(
                        owner,
                        member)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Active team membership not found"));
    }

    // Remove team member
    public void removeMember(
            Long memberId,
            String ownerEmail) {

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new RuntimeException("Owner not found"));

        TeamMember teamMember =
                teamMemberRepository.findById(memberId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Team member not found"));

        if (!teamMember.getOwner()
                .getId()
                .equals(owner.getId())) {

            throw new RuntimeException(
                    "Access denied");
        }

        teamMember.setActive(false);

        teamMemberRepository.save(teamMember);
    }
}