package com.securevault.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "team_members")
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Team owner / admin
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // Team member
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @Column(nullable = false)
    private String permission;

    @Column(nullable = false)
    private boolean active = true;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public User getMember() {
        return member;
    }

    public void setMember(User member) {
        this.member = member;
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
}