package com.securevault.backend.dto;

import com.securevault.backend.entity.Notification;

import java.time.LocalDateTime;

public class NotificationResponse {

    private Long id;
    private String type;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;

    public NotificationResponse() {
    }

    public NotificationResponse(
            Long id,
            String type,
            String message,
            boolean read,
            LocalDateTime createdAt) {

        this.id = id;
        this.type = type;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
    }

    public static NotificationResponse fromEntity(
            Notification notification) {

        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}