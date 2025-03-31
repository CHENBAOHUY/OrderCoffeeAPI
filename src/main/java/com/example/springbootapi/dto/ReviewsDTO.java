package com.example.springbootapi.dto;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class ReviewsDTO {
    private Integer id;
    private Integer orderId;
    private Integer userId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

}
