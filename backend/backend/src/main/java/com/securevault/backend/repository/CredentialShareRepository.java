package com.securevault.backend.repository;

import com.securevault.backend.entity.CredentialShare;
import com.securevault.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CredentialShareRepository
        extends JpaRepository<CredentialShare, Long> {

    List<CredentialShare> findBySharedUser(User user);

    List<CredentialShare> findByCredentialIdAndSharedUser(
            Long credentialId,
            User user
    );

    long countByActive(boolean active);
}