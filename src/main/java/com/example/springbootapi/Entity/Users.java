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

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true)
    private String phone;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Integer points = 0; // Giá trị mặc định

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String resetCode;
    private LocalDateTime resetExpiry;


    @Column(nullable = false)
    private String resetStatus;

    public enum Role {
        CUSTOMER, ADMIN
    }

    // Đặt giá trị mặc định trước khi lưu vào DB
    @PrePersist
    public void prePersist() {
        if (role == null) {
            role = Role.CUSTOMER;
        }
        if (resetStatus == null) {
            resetStatus = "Unused";
        }
    }
}
