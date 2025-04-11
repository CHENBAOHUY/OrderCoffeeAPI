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
    private Integer id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(unique = true, length = 50)
    private String phone;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer points = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50, columnDefinition = "NVARCHAR(50) DEFAULT 'Customer'")
    private Role role = Role.CUSTOMER;

    @Column(name = "reset_code", length = 255)
    private String resetCode;

    @Column(name = "reset_expiry")
    private LocalDateTime resetExpiry;

    @Column(name = "resetStatus", length = 50, nullable = false, columnDefinition = "NVARCHAR(50) DEFAULT 'Unused'")
    private String resetStatus = "Unused";

    @Column(name = "created_at", updatable = false, columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", columnDefinition = "BIT DEFAULT 0")
    private Boolean isDeleted = false; // Thay boolean bằng Boolean

    public Boolean isDeleted() { return isDeleted; } // Thay boolean bằng Boolean
    public void setDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; } // Thay boolean bằng Boolean

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