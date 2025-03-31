package com.example.springbootapi.Controller;

import com.example.springbootapi.Entity.Reviews;
import com.example.springbootapi.Service.ReviewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewsService reviewsService;

    @GetMapping
    public List<Reviews> getAllReviews() {
        return reviewsService.getAllReviews();
    }

    @GetMapping("/{id}")
    public Optional<Reviews> getReviewById(@PathVariable Integer id) {
        return reviewsService.getReviewById(id);
    }

    @PostMapping
    public Reviews addReview(@RequestBody Reviews review) {
        return reviewsService.addReview(review);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Integer id) {
        reviewsService.deleteReview(id);
    }
}
