package com.securevault.backend.controller;

import com.securevault.backend.entity.CredentialShare;
import com.securevault.backend.service.CredentialShareService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sharing")
public class CredentialShareController {

    private final CredentialShareService shareService;

    public CredentialShareController(
            CredentialShareService shareService) {
        this.shareService = shareService;
    }

    @PostMapping("/share")
    public ResponseEntity<?> shareCredential(
            @RequestParam Long credentialId,
            @RequestParam String sharedUserEmail,
            @RequestParam String permission,
            @RequestParam(required = false) String expiresAt,
            Authentication authentication) {

        try {
            String ownerEmail = authentication.getName();

            LocalDateTime expiry = null;

            if (expiresAt != null && !expiresAt.isBlank()) {
                expiry = LocalDateTime.parse(expiresAt);
            }

            CredentialShare share =
                    shareService.shareCredential(
                            credentialId,
                            ownerEmail,
                            sharedUserEmail,
                            permission,
                            expiry
                    );

            return ResponseEntity.ok(share);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @GetMapping("/shared-with-me")
    public ResponseEntity<?> getSharedCredentials(
            Authentication authentication) {

        try {
            String email = authentication.getName();

            List<CredentialShare> shares =
                    shareService.getSharedCredentials(email);

            return ResponseEntity.ok(shares);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @GetMapping("/my-shares")
    public ResponseEntity<?> getMyShares(
            Authentication authentication) {

        try {
            String ownerEmail = authentication.getName();

            List<CredentialShare> shares =
                    shareService.getMyShares(ownerEmail);

            return ResponseEntity.ok(shares);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/revoke")
    public ResponseEntity<?> revokeShare(
            @RequestParam Long credentialId,
            @RequestParam String sharedUserEmail,
            Authentication authentication) {

        try {
            String ownerEmail = authentication.getName();

            shareService.revokeShare(
                    credentialId,
                    ownerEmail,
                    sharedUserEmail
            );

            return ResponseEntity.ok(
                    "Credential sharing revoked successfully"
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