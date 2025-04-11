package com.example.springbootapi.Controller;

import com.example.springbootapi.dto.ReviewRequestDTO;
import com.example.springbootapi.dto.ReviewsDTO;
import com.example.springbootapi.Service.ReviewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewsService reviewsService;

    @PostMapping("/add")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReviewsDTO> addReview(@RequestBody ReviewRequestDTO reviewRequest) {
        ReviewsDTO review = reviewsService.addReview(
                reviewRequest.getUserId(),
                reviewRequest.getOrderId(),
                reviewRequest.getProductId(),
                reviewRequest.getRating(),
                reviewRequest.getComment()
        );
        return ResponseEntity.ok(review);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<List<ReviewsDTO>> getReviews(@PathVariable Integer productId) {
        return ResponseEntity.ok(reviewsService.getReviewsByProduct(productId));
    }
}