package com.example.springbootapi.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BannerDTO {
    private Integer id;
    private String title;
    private String imageUrl;
    private Boolean isActive;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}