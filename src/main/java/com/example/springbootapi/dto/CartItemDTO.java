package com.example.springbootapi.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CartItemDTO {
    private Integer id;
    private ProductResponseDTO product;
    private Integer quantity;
    private LocalDateTime addedAt;
}