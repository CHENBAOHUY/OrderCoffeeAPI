package com.example.springbootapi.dto;

import lombok.Data;
@Data
public class CartCheckoutRequest {
    private Integer userId;       // <-- bỏ trường này
    private String paymentMethod;
}