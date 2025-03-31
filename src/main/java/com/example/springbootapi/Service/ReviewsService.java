package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.Reviews;
import com.example.springbootapi.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewsService {

    @Autowired
    private ReviewRepository reviewsRepository;

    public List<Reviews> getAllReviews() {
        return reviewsRepository.findAll();
    }

    public Optional<Reviews> getReviewById(Integer id) {
        return reviewsRepository.findById(id);
    }

    public Reviews saveReview(Reviews review) {
        return reviewsRepository.save(review);
    }

    public void deleteReview(Integer id) {
        reviewsRepository.deleteById(id);
    }

    public Reviews addReview(Reviews review) {
        return reviewsRepository.save(review);
    }
}
