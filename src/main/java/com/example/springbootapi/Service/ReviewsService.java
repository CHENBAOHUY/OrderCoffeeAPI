package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.*;
import com.example.springbootapi.dto.ReviewsDTO;
import com.example.springbootapi.repository.ResourceNotFoundException;
import com.example.springbootapi.repository.OrdersRepository;
import com.example.springbootapi.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewsService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UsersService usersService;

    @Autowired
    private ProductsService productsService;

    @Autowired
    private OrdersRepository ordersRepository;

    // Phương thức mới để lấy đánh giá theo ID
    public ReviewsDTO getReviewById(Integer reviewId) {
        return reviewRepository.findById(reviewId)
                .map(ReviewsDTO::new)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá với ID: " + reviewId));
    }

    @Transactional
    public ReviewsDTO addReview(Integer userId, Integer orderId, Integer productId, Integer rating, String comment) {
        Users user = usersService.getUserById(userId);
        Products product = productsService.getProductById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        // Kiểm tra xem người dùng đã đánh giá sản phẩm này trong đơn hàng chưa
        if (reviewRepository.existsByOrderIdAndProductId(orderId, productId)) {
            throw new IllegalStateException("Bạn đã đánh giá sản phẩm này trong đơn hàng");
        }

        Reviews review = new Reviews();
        review.setUser(user);
        review.setProduct(product); // Giờ là Products, không phải Optional<Products> nữa
        review.setOrder(order);
        review.setRating(rating);
        review.setComment(comment);

        Reviews savedReview = reviewRepository.save(review);
        return new ReviewsDTO(savedReview);
    }
    public List<ReviewsDTO> getReviewsByProduct(Integer productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(ReviewsDTO::new)
                .collect(Collectors.toList());
    }

    public Page<ReviewsDTO> getReviewsByProduct(Integer productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable)
                .map(ReviewsDTO::new);
    }

    public Page<ReviewsDTO> getReviewsByUser(Integer userId, Pageable pageable) {
        return reviewRepository.findByUserId(userId, pageable)
                .map(ReviewsDTO::new);
    }

    // Lấy tất cả đánh giá
    public List<ReviewsDTO> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(ReviewsDTO::new)
                .collect(Collectors.toList());
    }

    // Cập nhật đánh giá
    @Transactional
    public ReviewsDTO updateReview(Integer reviewId, Integer userId, Integer rating, String comment) {
        Reviews review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá với ID: " + reviewId));

        // Kiểm tra xem người dùng có quyền cập nhật đánh giá này không
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Bạn không có quyền cập nhật đánh giá này");
        }

        review.setRating(rating);
        review.setComment(comment);

        Reviews updatedReview = reviewRepository.save(review);
        return new ReviewsDTO(updatedReview);
    }

    // Xóa đánh giá
    @Transactional
    public void deleteReview(Integer reviewId, Integer userId) {
        Reviews review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá với ID: " + reviewId));

        // Kiểm tra xem người dùng có quyền xóa đánh giá này không
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Bạn không có quyền xóa đánh giá này");
        }

        reviewRepository.delete(review);
    }

    // Lấy thống kê đánh giá của sản phẩm
    public Map<String, Object> getProductReviewStats(Integer productId) {
        Map<String, Object> stats = new HashMap<>();
        Double avgRating = reviewRepository.findAverageRatingByProductId(productId).orElse(0.0);
        Long totalReviews = reviewRepository.countByProductId(productId);

        stats.put("averageRating", avgRating);
        stats.put("totalReviews", totalReviews);

        return stats;
    }

    // Kiểm tra xem người dùng đã đánh giá sản phẩm trong đơn hàng chưa
    public boolean hasUserReviewedProduct(Integer userId, Integer productId, Integer orderId) {
        return reviewRepository.existsByOrderIdAndProductId(orderId, productId);
    }
}