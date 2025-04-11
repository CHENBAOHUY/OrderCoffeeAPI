package com.example.springbootapi.dto;

import lombok.Data;

@Data
public class ReviewRequestDTO {
    private Integer userId;
    private Integer orderId;
    private Integer productId;
    private Integer rating;
    private String comment;
}