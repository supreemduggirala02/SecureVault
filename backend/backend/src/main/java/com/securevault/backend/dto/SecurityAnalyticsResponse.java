package com.securevault.backend.dto;

import com.securevault.backend.entity.AuditLog;

import java.util.List;

public class SecurityAnalyticsResponse {

    private long successfulLogins;
    private long failedLogins;
    private long suspiciousActivities;
    private long activeSecurityAlerts;
    private List<AuditLog> recentActivity;

    public SecurityAnalyticsResponse() {
    }

    public SecurityAnalyticsResponse(
            long successfulLogins,
            long failedLogins,
            long suspiciousActivities,
            long activeSecurityAlerts,
            List<AuditLog> recentActivity) {

        this.successfulLogins = successfulLogins;
        this.failedLogins = failedLogins;
        this.suspiciousActivities = suspiciousActivities;
        this.activeSecurityAlerts = activeSecurityAlerts;
        this.recentActivity = recentActivity;
    }

    public long getSuccessfulLogins() {
        return successfulLogins;
    }

    public long getFailedLogins() {
        return failedLogins;
    }

    public long getSuspiciousActivities() {
        return suspiciousActivities;
    }

    public long getActiveSecurityAlerts() {
        return activeSecurityAlerts;
    }

    public List<AuditLog> getRecentActivity() {
        return recentActivity;
    }

    public void setSuccessfulLogins(long successfulLogins) {
        this.successfulLogins = successfulLogins;
    }

    public void setFailedLogins(long failedLogins) {
        this.failedLogins = failedLogins;
    }

    public void setSuspiciousActivities(long suspiciousActivities) {
        this.suspiciousActivities = suspiciousActivities;
    }

    public void setActiveSecurityAlerts(long activeSecurityAlerts) {
        this.activeSecurityAlerts = activeSecurityAlerts;
    }

    public void setRecentActivity(List<AuditLog> recentActivity) {
        this.recentActivity = recentActivity;
    }
}