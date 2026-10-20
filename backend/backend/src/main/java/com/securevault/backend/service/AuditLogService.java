package com.securevault.backend.service;

import com.securevault.backend.entity.AuditLog;
import com.securevault.backend.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(
            AuditLogRepository auditLogRepository) {

        this.auditLogRepository = auditLogRepository;
    }

    // Existing audit logging
    public AuditLog log(
            String userEmail,
            String action,
            String details) {

        AuditLog auditLog =
                new AuditLog(
                        userEmail,
                        action,
                        details,
                        null,
                        null,
                        null
                );

        return auditLogRepository.save(auditLog);
    }

    // Login monitoring
    public AuditLog logLogin(
            String userEmail,
            String loginStatus,
            String ipAddress,
            String userAgent,
            String details) {

        AuditLog auditLog =
                new AuditLog(
                        userEmail,
                        "LOGIN",
                        details,
                        loginStatus,
                        ipAddress,
                        userAgent
                );

        return auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getUserLogs(
            String userEmail) {

        return auditLogRepository
                .findByUserEmailOrderByTimestampDesc(
                        userEmail
                );
    }

    public List<AuditLog> getAllLogs() {

        return auditLogRepository
                .findAllByOrderByTimestampDesc();
    }
}