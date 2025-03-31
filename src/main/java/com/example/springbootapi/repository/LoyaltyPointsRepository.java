package com.example.springbootapi.repository;

import com.example.springbootapi.Entity.LoyaltyPoints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoyaltyPointsRepository extends JpaRepository<LoyaltyPoints, Integer> {
}
