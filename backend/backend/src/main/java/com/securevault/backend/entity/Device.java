package com.securevault.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "devices",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_device_user_device_id",
                        columnNames = {"user_id", "device_id"}
                )
        }
)
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_device_user")
    )
    private User user;

    @Column(
            name = "device_id",
            nullable = false,
            length = 100
    )
    private String deviceId;

    @Column(
            name = "device_name",
            length = 255
    )
    private String deviceName;

    @Column(
            name = "device_type",
            length = 100
    )
    private String deviceType;

    @Column(
            name = "ip_address",
            length = 100
    )
    private String ipAddress;

    @Column(
            name = "user_agent",
            length = 1000
    )
    private String userAgent;

    @Column(nullable = false)
    private boolean trusted = false;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastSeenAt;


    // =========================================================
    // PRE PERSIST
    // =========================================================

    @PrePersist
    protected void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (lastSeenAt == null) {
            lastSeenAt = now;
        }
    }


    // =========================================================
    // GETTERS / SETTERS
    // =========================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public boolean isTrusted() {
        return trusted;
    }

    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }
}