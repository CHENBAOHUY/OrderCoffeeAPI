package com.example.springbootapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BannerRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String imageUrl;

    @NotNull(message = "Active status is required")
    private Boolean isActive;
}