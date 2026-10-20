package com.securevault.backend.service;

import com.securevault.backend.dto.SecurityAnalyticsResponse;
import com.securevault.backend.entity.AuditLog;
import com.securevault.backend.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SecurityAnalyticsService {

    private final AuditLogRepository auditLogRepository;

    public SecurityAnalyticsService(
            AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public SecurityAnalyticsResponse getAnalytics(
            String userEmail,
            String range) {

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start;

        if (range == null || range.isBlank()) {
            range = "7";
        }

        switch (range.toLowerCase()) {

            case "today":
            case "1":
                start = LocalDate.now().atStartOfDay();
                break;

            case "7":
            case "last7":
                start = end.minusDays(7);
                break;

            case "30":
            case "last30":
                start = end.minusDays(30);
                break;

            default:
                start = end.minusDays(7);
                break;
        }

        List<AuditLog> logs =
                auditLogRepository
                        .findByUserEmailAndTimestampBetweenOrderByTimestampDesc(
                                userEmail,
                                start,
                                end
                        );

        long successfulLogins = logs.stream()
                .filter(log ->
                        "LOGIN_SUCCESS".equalsIgnoreCase(
                                log.getLoginStatus()
                        )
                )
                .count();

        long failedLogins = logs.stream()
                .filter(log ->
                        "LOGIN_FAILED".equalsIgnoreCase(
                                log.getLoginStatus()
                        )
                )
                .count();

        long suspiciousActivities = logs.stream()
                .filter(this::isSuspiciousActivity)
                .count();

        long activeSecurityAlerts = logs.stream()
                .filter(this::isSecurityAlert)
                .count();

        return new SecurityAnalyticsResponse(
                successfulLogins,
                failedLogins,
                suspiciousActivities,
                activeSecurityAlerts,
                logs
        );
    }

    private boolean isSuspiciousActivity(AuditLog log) {

        String action = log.getAction() == null
                ? ""
                : log.getAction().toUpperCase();

        String details = log.getDetails() == null
                ? ""
                : log.getDetails().toUpperCase();

        return action.contains("SUSPICIOUS")
                || details.contains("SUSPICIOUS");
    }

    private boolean isSecurityAlert(AuditLog log) {

        String action = log.getAction() == null
                ? ""
                : log.getAction().toUpperCase();

        String details = log.getDetails() == null
                ? ""
                : log.getDetails().toUpperCase();

        return action.contains("SECURITY_ALERT")
                || action.contains("SECURITY ALERT")
                || action.contains("ALERT")
                || details.contains("SECURITY_ALERT")
                || details.contains("SECURITY ALERT")
                || details.contains("SECURITY ALERT");
    }
}