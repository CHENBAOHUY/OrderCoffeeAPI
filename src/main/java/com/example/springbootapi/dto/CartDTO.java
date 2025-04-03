package com.example.springbootapi.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CartDTO {
    private Integer id;
    private UserDto user;
    private List<CartItemDTO> cartItems;
    private LocalDateTime createdAt;
}