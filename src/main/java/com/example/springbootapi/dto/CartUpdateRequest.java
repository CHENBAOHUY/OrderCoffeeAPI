package com.example.springbootapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CartUpdateRequest {
    @NotNull(message = "Cart item ID is required")
    private Integer cartItemId;

    @NotNull(message = "Product ID is required")
    private Integer productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;

    @NotNull(message = "Size is required")
    @Pattern(regexp = "^(S|M|L)$", message = "Size must be S, M, or L")
    private String size;
}