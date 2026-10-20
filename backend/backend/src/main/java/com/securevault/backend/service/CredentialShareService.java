package com.securevault.backend.service;

import com.securevault.backend.entity.Credential;
import com.securevault.backend.entity.CredentialShare;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.CredentialRepository;
import com.securevault.backend.repository.CredentialShareRepository;
import com.securevault.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CredentialShareService {

    private final CredentialShareRepository shareRepository;
    private final CredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public CredentialShareService(
            CredentialShareRepository shareRepository,
            CredentialRepository credentialRepository,
            UserRepository userRepository,
            NotificationService notificationService) {

        this.shareRepository = shareRepository;
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    // =========================================================
    // SHARE CREDENTIAL
    // =========================================================

    public CredentialShare shareCredential(
            Long credentialId,
            String ownerEmail,
            String sharedUserEmail,
            String permission,
            LocalDateTime expiresAt) {

        User owner =
                userRepository.findByEmailIgnoreCase(ownerEmail)
                        .orElseThrow(() ->
                                new RuntimeException("Owner not found"));

        User sharedUser =
                userRepository.findByEmailIgnoreCase(sharedUserEmail)
                        .orElseThrow(() ->
                                new RuntimeException("Shared user not found"));

        Credential credential =
                credentialRepository.findById(credentialId)
                        .orElseThrow(() ->
                                new RuntimeException("Credential not found"));

        // Only owner can share
        if (!credential.getUser().getId()
                .equals(owner.getId())) {

            throw new RuntimeException("Access denied");
        }

        // Prevent sharing with yourself
        if (owner.getId().equals(sharedUser.getId())) {
            throw new RuntimeException(
                    "You cannot share a credential with yourself");
        }

        // Normalize permission
        permission = permission == null
                ? null
                : permission.trim().toUpperCase();

        // Validate permission
        if (!"VIEW_ONLY".equals(permission)
                && !"EDIT".equals(permission)
                && !"FULL_MANAGEMENT".equals(permission)) {

            throw new RuntimeException("Invalid permission");
        }

        // Validate expiry
        if (expiresAt != null
                && !expiresAt.isAfter(LocalDateTime.now())) {

            throw new RuntimeException(
                    "Expiry time must be in the future");
        }

        // Find previous shares
        List<CredentialShare> existingShares =
                shareRepository.findByCredentialIdAndSharedUser(
                        credentialId,
                        sharedUser
                );

        // Deactivate old active shares
        for (CredentialShare existing : existingShares) {

            if (existing.isActive()) {
                existing.setActive(false);
                shareRepository.save(existing);
            }
        }

        // Create new share
        CredentialShare share = new CredentialShare();

        share.setCredential(credential);
        share.setSharedUser(sharedUser);
        share.setPermission(permission);
        share.setActive(true);
        share.setExpiresAt(expiresAt);

        CredentialShare savedShare =
                shareRepository.save(share);

        // Create notification for shared user
        String expiryMessage = expiresAt == null
                ? ""
                : " The access expires at " + expiresAt + ".";

        notificationService.createNotification(
                sharedUserEmail,
                "CREDENTIAL_SHARED",
                "A credential \"" + credential.getTitle()
                        + "\" has been shared with you with "
                        + permission
                        + " permission."
                        + expiryMessage
        );

        return savedShare;
    }

    // =========================================================
    // GET CREDENTIALS SHARED WITH CURRENT USER
    // =========================================================

    public List<CredentialShare> getSharedCredentials(
            String userEmail) {

        User user =
                userRepository.findByEmailIgnoreCase(userEmail)
                        .orElseThrow(() ->
                                new RuntimeException("User not found"));

        List<CredentialShare> shares =
                shareRepository.findBySharedUser(user);

        LocalDateTime now =
                LocalDateTime.now();

        // Deactivate expired shares
        for (CredentialShare share : shares) {

            if (share.isActive()
                    && share.getExpiresAt() != null
                    && !share.getExpiresAt().isAfter(now)) {

                share.setActive(false);
                shareRepository.save(share);
            }
        }

        // Return only active shares
        return shares.stream()
                .filter(CredentialShare::isActive)
                .toList();
    }

    // =========================================================
    // GET SHARES CREATED BY CURRENT OWNER
    // =========================================================

    public List<CredentialShare> getMyShares(
            String ownerEmail) {

        User owner =
                userRepository.findByEmailIgnoreCase(ownerEmail)
                        .orElseThrow(() ->
                                new RuntimeException("Owner not found"));

        // Get all shares
        List<CredentialShare> allShares =
                shareRepository.findAll();

        LocalDateTime now =
                LocalDateTime.now();

        // Deactivate expired shares
        for (CredentialShare share : allShares) {

            if (share.isActive()
                    && share.getExpiresAt() != null
                    && !share.getExpiresAt().isAfter(now)) {

                share.setActive(false);
                shareRepository.save(share);
            }
        }

        // Return only active shares
        // belonging to credentials owned by this user
        return allShares.stream()
                .filter(share ->
                        share.getCredential() != null
                        && share.getCredential().getUser() != null
                        && share.getCredential()
                                .getUser()
                                .getId()
                                .equals(owner.getId())
                        && share.isActive()
                )
                .toList();
    }

    // =========================================================
    // GET PERMISSION
    // =========================================================

    public String getPermission(
            Long credentialId,
            String userEmail) {

        User user =
                userRepository.findByEmailIgnoreCase(userEmail)
                        .orElseThrow(() ->
                                new RuntimeException("User not found"));

        List<CredentialShare> shares =
                shareRepository.findByCredentialIdAndSharedUser(
                        credentialId,
                        user
                );

        if (shares.isEmpty()) {
            throw new RuntimeException("Access denied");
        }

        LocalDateTime now =
                LocalDateTime.now();

        for (CredentialShare share : shares) {

            if (!share.isActive()) {
                continue;
            }

            if (share.getExpiresAt() != null
                    && !share.getExpiresAt().isAfter(now)) {

                share.setActive(false);
                shareRepository.save(share);

                continue;
            }

            return share.getPermission();
        }

        throw new RuntimeException("Access denied");
    }

    // =========================================================
    // REVOKE SHARE
    // =========================================================

    public void revokeShare(
            Long credentialId,
            String ownerEmail,
            String sharedUserEmail) {

        User owner =
                userRepository.findByEmailIgnoreCase(ownerEmail)
                        .orElseThrow(() ->
                                new RuntimeException("Owner not found"));

        User sharedUser =
                userRepository.findByEmailIgnoreCase(sharedUserEmail)
                        .orElseThrow(() ->
                                new RuntimeException("Shared user not found"));

        Credential credential =
                credentialRepository.findById(credentialId)
                        .orElseThrow(() ->
                                new RuntimeException("Credential not found"));

        // Only owner can revoke
        if (!credential.getUser().getId()
                .equals(owner.getId())) {

            throw new RuntimeException("Access denied");
        }

        List<CredentialShare> shares =
                shareRepository.findByCredentialIdAndSharedUser(
                        credentialId,
                        sharedUser
                );

        if (shares.isEmpty()) {
            throw new RuntimeException("Share not found");
        }

        boolean foundActiveShare = false;

        for (CredentialShare share : shares) {

            if (share.isActive()) {

                share.setActive(false);
                shareRepository.save(share);

                foundActiveShare = true;
            }
        }

        if (!foundActiveShare) {
            throw new RuntimeException("Share already revoked");
        }

        // Notify shared user
        notificationService.createNotification(
                sharedUserEmail,
                "CREDENTIAL_SHARE_REVOKED",
                "Your access to credential \""
                        + credential.getTitle()
                        + "\" has been revoked."
        );
    }
}