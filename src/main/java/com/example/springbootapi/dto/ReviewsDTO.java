package com.example.springbootapi.dto;

import com.example.springbootapi.Entity.Reviews;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewsDTO {
    private Integer id;
    private Integer userId;
    private String userName;
    private Integer productId;
    private String productName;
    private Integer orderId;
    private Integer rating;
    private String comment;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ReviewsDTO(Reviews review) {
        this.id = review.getId();
        this.userId = review.getUser().getId();
        this.userName = review.getUser().getName();
        this.productId = review.getProduct().getId();
        this.productName = review.getProduct().getName();
        this.orderId = review.getOrder().getId();
        this.rating = review.getRating();
        this.comment = review.getComment();
        this.title = review.getTitle();
        this.createdAt = review.getCreatedAt();
        this.updatedAt = review.getUpdatedAt();
    }
}