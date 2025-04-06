package com.example.springbootapi.dto;

import lombok.Data;

@Data
public class CartCheckoutRequest {
    private Integer userId;
    private String paymentMethod;
}