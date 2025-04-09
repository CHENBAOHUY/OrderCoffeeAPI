package com.example.springbootapi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartAddRequest {
    private Integer userId;

    @NotNull(message = "Product ID is required")
    private Integer productId;

    @NotNull(message = "Quantity is required")
    private Integer quantity;
}