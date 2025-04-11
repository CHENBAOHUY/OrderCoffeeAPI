package com.example.springbootapi.repository;

import com.example.springbootapi.Entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Integer> {
    List<PriceHistory> findByProductId(Integer productId);
}