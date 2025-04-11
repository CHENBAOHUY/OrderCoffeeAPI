package com.example.springbootapi.repository;

import com.example.springbootapi.Entity.LoyaltyPoints;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoyaltyPointsRepository extends JpaRepository<LoyaltyPoints, Integer> {
    List<LoyaltyPoints> findByUserId(Integer userId);
}