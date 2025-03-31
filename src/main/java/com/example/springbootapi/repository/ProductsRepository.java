package com.example.springbootapi.repository;

import com.example.springbootapi.Entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductsRepository extends JpaRepository<Products, Integer> {
    List<Products> findByCategoriesId(Integer categoryId);
}
