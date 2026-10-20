package com.securevault.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "credential_shares")
public class CredentialShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credential_id", nullable = false)
    private Credential credential;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_user_id", nullable = false)
    private User sharedUser;

    @Column(nullable = false)
    private String permission;

    @Column(nullable = false)
    private boolean active = true;

    private LocalDateTime expiresAt;

    public CredentialShare() {
    }

    public Long getId() {
        return id;
    }

    public Credential getCredential() {
        return credential;
    }

    public void setCredential(Credential credential) {
        this.credential = credential;
    }

    public User getSharedUser() {
        return sharedUser;
    }

    public void setSharedUser(User sharedUser) {
        this.sharedUser = sharedUser;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}