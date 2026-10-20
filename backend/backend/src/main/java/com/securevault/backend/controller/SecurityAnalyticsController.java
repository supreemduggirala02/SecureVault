package com.securevault.backend.controller;

import com.securevault.backend.dto.SecurityAnalyticsResponse;
import com.securevault.backend.service.SecurityAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class SecurityAnalyticsController {

    private final SecurityAnalyticsService securityAnalyticsService;

    public SecurityAnalyticsController(
            SecurityAnalyticsService securityAnalyticsService) {
        this.securityAnalyticsService = securityAnalyticsService;
    }

    @GetMapping("/security")
    public ResponseEntity<?> getSecurityAnalytics(
            Authentication authentication,
            @RequestParam(defaultValue = "7") String range) {

        try {
            String userEmail = authentication.getName();

            SecurityAnalyticsResponse analytics =
                    securityAnalyticsService.getAnalytics(
                            userEmail,
                            range
                    );

            return ResponseEntity.ok(analytics);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}