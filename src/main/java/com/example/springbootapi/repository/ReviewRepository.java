package com.example.springbootapi.repository;

import com.example.springbootapi.Entity.Reviews;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Reviews, Integer> {

    List<Reviews> findByProductId(Integer productId);

    Page<Reviews> findByProductId(Integer productId, Pageable pageable);

    List<Reviews> findByUserId(Integer userId);

    Page<Reviews> findByUserId(Integer userId, Pageable pageable);

    boolean existsByOrderIdAndProductId(Integer orderId, Integer productId);

    @Query("SELECT AVG(r.rating) FROM Reviews r WHERE r.product.id = :productId")
    Optional<Double> findAverageRatingByProductId(@Param("productId") Integer productId);

    @Query("SELECT COUNT(r) FROM Reviews r WHERE r.product.id = :productId")
    Long countByProductId(@Param("productId") Integer productId);
}