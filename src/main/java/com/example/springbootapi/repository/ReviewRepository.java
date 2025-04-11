package com.example.springbootapi.repository;

import com.example.springbootapi.Entity.Reviews;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Reviews, Integer> {
    List<Reviews> findByProductId(Integer productId);
    List<Reviews> findByOrderId(Integer orderId); // Thêm để lấy review theo đơn hàng
    boolean existsByOrderIdAndProductId(Integer orderId, Integer productId); // Kiểm tra đã review chưa
}