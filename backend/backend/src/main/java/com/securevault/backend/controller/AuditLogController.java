package com.securevault.backend.controller;

import com.securevault.backend.entity.AuditLog;
import com.securevault.backend.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(
            AuditLogService auditLogService) {

        this.auditLogService = auditLogService;
    }

    // =========================
    // MY LOGIN HISTORY
    // =========================

    @GetMapping("/login-history")
    public ResponseEntity<?> getMyLoginHistory(
            Authentication authentication) {

        try {

            // Get currently logged-in user's email
            String email = authentication.getName();

            // Temporary debug message
            System.out.println(
                    "LOGIN HISTORY USER = " + email
            );

            // Fetch only this user's login history
            List<AuditLog> logs =
                    auditLogService.getUserLogs(email);

            return ResponseEntity.ok(logs);

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}