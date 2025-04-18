package com.example.springbootapi.repository;

import com.example.springbootapi.Entity.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Trong OrderDetailsRepository.java
public interface OrderDetailsRepository extends JpaRepository<OrderDetails, Integer> {
    List<OrderDetails> findByOrderId(Integer orderId);
    // Hoặc nếu sử dụng quan hệ JPA:
    List<OrderDetails> findByOrder_Id(Integer orderId);
}