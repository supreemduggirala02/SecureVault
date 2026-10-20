package com.securevault.backend.service;

import com.securevault.backend.entity.AuditLog;
import com.securevault.backend.repository.AuditLogRepository;
import com.securevault.backend.repository.CredentialRepository;
import com.securevault.backend.repository.CredentialShareRepository;
import com.securevault.backend.repository.NotificationRepository;
import com.securevault.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final CredentialShareRepository credentialShareRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;

    public ReportService(
            UserRepository userRepository,
            CredentialRepository credentialRepository,
            CredentialShareRepository credentialShareRepository,
            NotificationRepository notificationRepository,
            AuditLogRepository auditLogRepository) {

        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.credentialShareRepository = credentialShareRepository;
        this.notificationRepository = notificationRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public Map<String, Object> getSystemReport() {

        Map<String, Object> report = new LinkedHashMap<>();

        report.put("totalUsers", userRepository.count());
        report.put("totalCredentials", credentialRepository.count());
        report.put("totalShares", credentialShareRepository.count());
        report.put("activeShares", credentialShareRepository.countByActive(true));
        report.put("totalNotifications", notificationRepository.count());
        report.put("unreadNotifications", notificationRepository.countByReadFalse());
        report.put("totalAuditLogs", auditLogRepository.count());

        return report;
    }

    public List<AuditLog> getAuditLogs(
            LocalDate startDate,
            LocalDate endDate) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        return auditLogRepository
                .findAllByOrderByTimestampDesc()
                .stream()
                .filter(log ->
                        log.getTimestamp() != null
                                && !log.getTimestamp().isBefore(start)
                                && !log.getTimestamp().isAfter(end))
                .toList();
    }

    public List<AuditLog> getUserAuditLogs(
            String email,
            LocalDate startDate,
            LocalDate endDate) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        return auditLogRepository
                .findByUserEmailAndTimestampBetweenOrderByTimestampDesc(
                        email,
                        start,
                        end
                );
    }

    public Map<String, Object> getUserSecurityReport(
            String email,
            LocalDate startDate,
            LocalDate endDate) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        long successfulLogins =
                auditLogRepository.countByUserEmailAndLoginStatusAndTimestampBetween(
                        email,
                        "SUCCESS",
                        start,
                        end
                );

        long failedLogins =
                auditLogRepository.countByUserEmailAndLoginStatusAndTimestampBetween(
                        email,
                        "FAILED",
                        start,
                        end
                );

        Map<String, Object> report = new LinkedHashMap<>();

        report.put("email", email);
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("successfulLogins", successfulLogins);
        report.put("failedLogins", failedLogins);

        return report;
    }
}