package com.example.springbootapi.dto;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class CartDTO {
    private Integer id;
    private Integer userId;
    private Integer productId;
    private Integer quantity;
    private LocalDateTime addedAt;

}
