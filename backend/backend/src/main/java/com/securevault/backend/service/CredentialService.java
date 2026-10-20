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
public class CredentialService {

    private final CredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final CredentialShareRepository shareRepository;
    private final EncryptionService encryptionService;
    private final AuditLogService auditLogService;

    public CredentialService(
            CredentialRepository credentialRepository,
            UserRepository userRepository,
            CredentialShareRepository shareRepository,
            EncryptionService encryptionService,
            AuditLogService auditLogService) {

        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
        this.shareRepository = shareRepository;
        this.encryptionService = encryptionService;
        this.auditLogService = auditLogService;
    }

    // =========================================================
    // ADD CREDENTIAL
    // =========================================================

    public Credential addCredential(
            Credential credential,
            String email) {

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        validateCredentialInput(credential, true);

        credential.setUser(user);

        credential.setPassword(
                encryptionService.encrypt(
                        credential.getPassword()
                )
        );

        Credential savedCredential =
                credentialRepository.save(credential);

        auditLogService.log(
                email,
                "CREATE_CREDENTIAL",
                "Created credential: "
                        + savedCredential.getTitle()
        );

        /*
         * IMPORTANT:
         * Do NOT decrypt password before returning.
         * Database password remains encrypted.
         */
        savedCredential.setPassword(null);

        return savedCredential;
    }

    // =========================================================
    // GET MY CREDENTIALS
    // =========================================================

    public List<Credential> getMyCredentials(
            String email) {

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        List<Credential> credentials =
                credentialRepository.findByUser(user);

        /*
         * Never return encrypted or decrypted password.
         */
        for (Credential credential : credentials) {
            credential.setPassword(null);
        }

        return credentials;
    }

    // =========================================================
    // GET SINGLE CREDENTIAL
    // =========================================================

    public Credential getCredential(
            Long id,
            String email) {

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Credential credential =
                credentialRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Credential not found"));

        boolean isOwner =
                credential.getUser()
                        .getId()
                        .equals(user.getId());

        if (!isOwner) {

            CredentialShare share =
                    findActiveShare(id, user);

            checkShareAccess(share);
        }

        /*
         * Never expose password through API.
         */
        credential.setPassword(null);

        return credential;
    }

    // =========================================================
    // UPDATE CREDENTIAL
    // =========================================================

    public Credential updateCredential(
            Long id,
            Credential updatedCredential,
            String email) {

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Credential credential =
                credentialRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Credential not found"));

        boolean isOwner =
                credential.getUser()
                        .getId()
                        .equals(user.getId());

        if (!isOwner) {

            CredentialShare share =
                    findActiveShare(id, user);

            checkShareAccess(share);

            String permission =
                    share.getPermission();

            if (!"EDIT".equals(permission)
                    && !"FULL_MANAGEMENT".equals(permission)) {

                throw new RuntimeException(
                        "Edit permission required");
            }
        }

        /*
         * Title and username are always required.
         */
        if (updatedCredential.getTitle() == null
                || updatedCredential.getTitle().isBlank()) {

            throw new RuntimeException(
                    "Title cannot be empty");
        }

        if (updatedCredential.getUsername() == null
                || updatedCredential.getUsername().isBlank()) {

            throw new RuntimeException(
                    "Username cannot be empty");
        }

        credential.setTitle(
                updatedCredential.getTitle().trim()
        );

        credential.setUsername(
                updatedCredential.getUsername().trim()
        );

        /*
         * Password is optional during update.
         *
         * If user enters a new password:
         *     encrypt and replace old password.
         *
         * If password is blank:
         *     keep existing encrypted password.
         */
        if (updatedCredential.getPassword() != null
                && !updatedCredential.getPassword().isBlank()) {

            credential.setPassword(
                    encryptionService.encrypt(
                            updatedCredential.getPassword()
                    )
            );
        }

        Credential savedCredential =
                credentialRepository.save(credential);

        auditLogService.log(
                email,
                "UPDATE_CREDENTIAL",
                "Updated credential: "
                        + savedCredential.getTitle()
        );

        /*
         * Never return encrypted/plaintext password.
         */
        savedCredential.setPassword(null);

        return savedCredential;
    }

    // =========================================================
    // DELETE CREDENTIAL
    // =========================================================

    public void deleteCredential(
            Long id,
            String email) {

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Credential credential =
                credentialRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Credential not found"));

        boolean isOwner =
                credential.getUser()
                        .getId()
                        .equals(user.getId());

        if (!isOwner) {

            CredentialShare share =
                    findActiveShare(id, user);

            checkShareAccess(share);

            if (!"FULL_MANAGEMENT"
                    .equals(share.getPermission())) {

                throw new RuntimeException(
                        "Full management permission required");
            }
        }

        String credentialTitle =
                credential.getTitle();

        credentialRepository.delete(credential);

        auditLogService.log(
                email,
                "DELETE_CREDENTIAL",
                "Deleted credential: "
                        + credentialTitle
        );
    }

    // =========================================================
    // VALIDATE CREDENTIAL INPUT
    // =========================================================

    private void validateCredentialInput(
            Credential credential,
            boolean requirePassword) {

        if (credential == null) {
            throw new RuntimeException(
                    "Credential data is required");
        }

        if (credential.getTitle() == null
                || credential.getTitle().isBlank()) {

            throw new RuntimeException(
                    "Title cannot be empty");
        }

        if (credential.getUsername() == null
                || credential.getUsername().isBlank()) {

            throw new RuntimeException(
                    "Username cannot be empty");
        }

        if (requirePassword
                && (credential.getPassword() == null
                || credential.getPassword().isBlank())) {

            throw new RuntimeException(
                    "Password cannot be empty");
        }
    }

    // =========================================================
    // FIND ACTIVE SHARE
    // =========================================================

    private CredentialShare findActiveShare(
            Long credentialId,
            User user) {

        List<CredentialShare> shares =
                shareRepository
                        .findByCredentialIdAndSharedUser(
                                credentialId,
                                user
                        );

        if (shares.isEmpty()) {
            throw new RuntimeException(
                    "Access denied");
        }

        LocalDateTime now =
                LocalDateTime.now();

        for (CredentialShare share : shares) {

            if (!share.isActive()) {
                continue;
            }

            if (share.getExpiresAt() != null
                    && !share.getExpiresAt()
                    .isAfter(now)) {

                share.setActive(false);
                shareRepository.save(share);

                continue;
            }

            return share;
        }

        throw new RuntimeException(
                "Access denied");
    }

    // =========================================================
    // CHECK SHARE ACCESS
    // =========================================================

    private void checkShareAccess(
            CredentialShare share) {

        if (share == null) {
            throw new RuntimeException(
                    "Access denied");
        }

        if (!share.isActive()) {
            throw new RuntimeException(
                    "Access denied");
        }

        if (share.getExpiresAt() != null
                && !share.getExpiresAt()
                .isAfter(LocalDateTime.now())) {

            share.setActive(false);
            shareRepository.save(share);

            throw new RuntimeException(
                    "Access expired");
        }
    }
}