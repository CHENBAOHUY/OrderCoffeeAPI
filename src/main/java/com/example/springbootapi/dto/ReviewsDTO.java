package com.example.springbootapi.dto;

import com.example.springbootapi.Entity.Reviews;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewsDTO {
    private Integer id;
    private Integer userId;
    private String userName;
    private Integer productId;
    private Integer orderId; // Thêm orderId
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    public ReviewsDTO(Reviews review) {
        this.id = review.getId();
        this.userId = review.getUser().getId();
        this.userName = review.getUser().getName();
        this.productId = review.getProduct().getId();
        this.orderId = review.getOrder().getId();
        this.rating = review.getRating();
        this.comment = review.getComment();
        this.createdAt = review.getCreatedAt();
    }
}