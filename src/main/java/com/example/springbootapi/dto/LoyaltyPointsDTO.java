package com.example.springbootapi.dto;

import com.example.springbootapi.Entity.LoyaltyPoints;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoyaltyPointsDTO {
    private Integer id;
    private Integer userId;
    private String userName;
    private Integer points;
    private String source;
    private LocalDateTime earnedAt;
    private LocalDateTime expiresAt;

    public LoyaltyPointsDTO(LoyaltyPoints loyaltyPoints) {
        this.id = loyaltyPoints.getId();
        this.userId = loyaltyPoints.getUser().getId();
        this.userName = loyaltyPoints.getUser().getName();
        this.points = loyaltyPoints.getPoints();
        this.source = loyaltyPoints.getSource();
        this.earnedAt = loyaltyPoints.getEarnedAt();
        this.expiresAt = loyaltyPoints.getExpiresAt();
    }
}