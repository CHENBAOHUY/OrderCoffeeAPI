package com.example.springbootapi.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "LoyaltyPoints")
public class LoyaltyPoints {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(nullable = false)
    private Integer points;

    @Column(length = 50)
    private String source;

    @Column(name = "earned_at", nullable = false, columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime earnedAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}