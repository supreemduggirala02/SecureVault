package com.securevault.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    // =========================
    // MFA
    // =========================

    @Column(nullable = false)
    private boolean mfaEnabled = false;

    @Column(length = 255)
    private String mfaSecret;

    // =========================
    // PASSWORD RECOVERY
    // =========================

    @Column(length = 255)
    private String resetToken;

    @Column
    private LocalDateTime resetTokenExpiry;

    // =========================
    // RBAC ROLE
    // =========================

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private Role role = Role.USER;

    // =========================
    // GETTERS
    // =========================

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public boolean isMfaEnabled() {
        return mfaEnabled;
    }

    public String getMfaSecret() {
        return mfaSecret;
    }

    public String getResetToken() {
        return resetToken;
    }

    public LocalDateTime getResetTokenExpiry() {
        return resetTokenExpiry;
    }

    public Role getRole() {
        return role;
    }

    // =========================
    // SETTERS
    // =========================

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setMfaEnabled(boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
    }

    public void setMfaSecret(String mfaSecret) {
        this.mfaSecret = mfaSecret;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) {
        this.resetTokenExpiry = resetTokenExpiry;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}