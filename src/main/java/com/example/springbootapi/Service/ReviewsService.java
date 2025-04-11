package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.*;
import com.example.springbootapi.dto.ReviewsDTO;
import com.example.springbootapi.repository.OrdersRepository;
import com.example.springbootapi.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewsService {

    @Autowired
    private ReviewRepository reviewsRepository;

    @Autowired
    private UsersService usersService;

    @Autowired
    private ProductsService productsService;

    @Autowired
    private OrdersRepository ordersRepository;

    @Transactional
    public ReviewsDTO addReview(Integer userId, Integer orderId, Integer productId, Integer rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Điểm đánh giá phải từ 1 đến 5");
        }

        Users user = usersService.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId);
        }

        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + orderId));
        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Đơn hàng không thuộc về người dùng này");
        }
        if (!order.getStatus().equals(Orders.OrderStatus.COMPLETED)) {
            throw new IllegalArgumentException("Đơn hàng chưa hoàn tất, không thể đánh giá");
        }

        Products product = productsService.getProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + productId));

        // Kiểm tra sản phẩm có trong đơn hàng không (giả sử OrderDetails lưu chi tiết)
        boolean productInOrder = order.getOrderDetails().stream()
                .anyMatch(detail -> detail.getProduct().getId().equals(productId));
        if (!productInOrder) {
            throw new IllegalArgumentException("Sản phẩm không thuộc đơn hàng này");
        }

        // Kiểm tra đã review chưa
        if (reviewsRepository.existsByOrderIdAndProductId(orderId, productId)) {
            throw new IllegalArgumentException("Sản phẩm này trong đơn hàng đã được đánh giá");
        }

        Reviews review = new Reviews();
        review.setUser(user);
        review.setProduct(product);
        review.setOrder(order);
        review.setRating(rating);
        review.setComment(comment);
        Reviews savedReview = reviewsRepository.save(review);
        return new ReviewsDTO(savedReview);
    }

    public List<ReviewsDTO> getReviewsByProduct(Integer productId) {
        return reviewsRepository.findByProductId(productId)
                .stream()
                .map(ReviewsDTO::new)
                .collect(Collectors.toList());
    }
}