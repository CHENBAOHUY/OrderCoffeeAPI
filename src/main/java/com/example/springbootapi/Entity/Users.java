package com.example.springbootapi.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "phone", unique = true, length = 50)
    private String phone;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "points", columnDefinition = "INT DEFAULT 0")
    private Integer points = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private Role role;

    @Column(name = "reset_code", length = 255)
    private String resetCode;

    @Column(name = "reset_expiry")
    private LocalDateTime resetExpiry;

    @Column(name = "reset_status", length = 50)
    private String resetStatus;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Role {
        CUSTOMER, ADMIN
    }

    @PrePersist
    public void prePersist() {
        if (role == null) role = Role.CUSTOMER;
        if (resetStatus == null) resetStatus = "Unused";
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}