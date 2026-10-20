package com.securevault.backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> adminDashboard() {

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Admin access granted",
                        "role",
                        "ADMINISTRATOR"
                )
        );
    }
}