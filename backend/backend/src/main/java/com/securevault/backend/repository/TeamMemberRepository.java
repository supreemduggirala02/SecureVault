package com.securevault.backend.repository;

import com.securevault.backend.entity.TeamMember;
import com.securevault.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository
        extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findByOwner(User owner);

    List<TeamMember> findByMember(User member);

    Optional<TeamMember> findByOwnerAndMember(
            User owner,
            User member
    );

    Optional<TeamMember> findByOwnerAndMemberAndActiveTrue(
            User owner,
            User member
    );
}