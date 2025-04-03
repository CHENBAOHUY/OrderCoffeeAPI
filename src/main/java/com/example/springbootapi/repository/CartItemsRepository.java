package com.example.springbootapi.repository;

import com.example.springbootapi.Entity.CartItems;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemsRepository extends JpaRepository<CartItems, Integer> {
    Optional<CartItems> findByCart_IdAndProduct_Id(Integer cartId, Integer productId);
}