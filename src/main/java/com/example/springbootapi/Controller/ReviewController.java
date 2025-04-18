package com.example.springbootapi.Controller;

import com.example.springbootapi.Service.ReviewsService;
import com.example.springbootapi.dto.ReviewsDTO;
import com.example.springbootapi.dto.ReviewRequestDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewsService reviewsService;

    public ReviewController(ReviewsService reviewsService) {
        this.reviewsService = reviewsService;
    }

    @GetMapping
    public ResponseEntity<List<ReviewsDTO>> getAllReviews() {
        return ResponseEntity.ok(reviewsService.getAllReviews());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewsDTO> getReviewById(@PathVariable Integer id) {
        return ResponseEntity.ok(reviewsService.getReviewById(id));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<ReviewsDTO>> getReviewsByProduct(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(reviewsService.getReviewsByProduct(productId, pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ReviewsDTO>> getReviewsByUser(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(reviewsService.getReviewsByUser(userId, pageable));
    }

    @PostMapping("/add")
    public ResponseEntity<ReviewsDTO> addReview(@Valid @RequestBody ReviewRequestDTO request) {
        ReviewsDTO reviewsDTO = reviewsService.addReview(
                request.getUserId(),
                request.getOrderId(),
                request.getProductId(),
                request.getRating(),
                request.getComment()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewsDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewsDTO> updateReview(
            @PathVariable Integer id,
            @Valid @RequestBody ReviewRequestDTO request,
            @RequestParam Integer userId) {
        ReviewsDTO reviewsDTO = reviewsService.updateReview(
                id,
                userId,
                request.getRating(),
                request.getComment()
        );
        return ResponseEntity.ok(reviewsDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Integer id,
            @RequestParam Integer userId) {
        reviewsService.deleteReview(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/product/{productId}/stats")
    public ResponseEntity<Map<String, Object>> getProductReviewStats(@PathVariable Integer productId) {
        return ResponseEntity.ok(reviewsService.getProductReviewStats(productId));
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkUserReview(
            @RequestParam Integer userId,
            @RequestParam Integer productId,
            @RequestParam Integer orderId) {
        return ResponseEntity.ok(reviewsService.hasUserReviewedProduct(userId, productId, orderId));
    }
}