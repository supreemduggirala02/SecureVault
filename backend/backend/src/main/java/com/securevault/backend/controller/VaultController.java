package com.securevault.backend.controller;

import com.securevault.backend.entity.Credential;
import com.securevault.backend.service.CredentialService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vault")
public class VaultController {

    private final CredentialService credentialService;

    public VaultController(CredentialService credentialService) {
        this.credentialService = credentialService;
    }

    @PostMapping("/credentials")
    public ResponseEntity<?> addCredential(
            @RequestBody Credential credential,
            Authentication authentication) {

        try {
            String email = authentication.getName();

            Credential savedCredential =
                    credentialService.addCredential(
                            credential,
                            email
                    );

            return ResponseEntity.ok(savedCredential);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @GetMapping("/credentials")
    public ResponseEntity<?> getCredentials(
            Authentication authentication) {

        try {
            String email = authentication.getName();

            List<Credential> credentials =
                    credentialService.getMyCredentials(email);

            return ResponseEntity.ok(credentials);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @GetMapping("/credentials/{id}")
    public ResponseEntity<?> getCredential(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            String email = authentication.getName();

            Credential credential =
                    credentialService.getCredential(
                            id,
                            email
                    );

            return ResponseEntity.ok(credential);

        } catch (RuntimeException e) {

            if ("Access denied".equals(e.getMessage())
                    || "Access expired".equals(e.getMessage())) {

                return ResponseEntity.status(403)
                        .body(e.getMessage());
            }

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @PutMapping("/credentials/{id}")
    public ResponseEntity<?> updateCredential(
            @PathVariable Long id,
            @RequestBody Credential credential,
            Authentication authentication) {

        try {
            String email = authentication.getName();

            Credential updatedCredential =
                    credentialService.updateCredential(
                            id,
                            credential,
                            email
                    );

            return ResponseEntity.ok(updatedCredential);

        } catch (RuntimeException e) {

            if ("Access denied".equals(e.getMessage())
                    || "Access expired".equals(e.getMessage())
                    || "Edit permission required".equals(e.getMessage())) {

                return ResponseEntity.status(403)
                        .body(e.getMessage());
            }

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/credentials/{id}")
    public ResponseEntity<?> deleteCredential(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            String email = authentication.getName();

            credentialService.deleteCredential(
                    id,
                    email
            );

            return ResponseEntity.ok(
                    "Credential deleted successfully"
            );

        } catch (RuntimeException e) {

            if ("Access denied".equals(e.getMessage())
                    || "Access expired".equals(e.getMessage())
                    || "Full management permission required"
                    .equals(e.getMessage())) {

                return ResponseEntity.status(403)
                        .body(e.getMessage());
            }

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}