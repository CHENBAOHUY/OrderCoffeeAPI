package com.example.springbootapi.repository;

import com.example.springbootapi.Entity.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriesRepository extends JpaRepository<Categories, Integer> {
    @Override
    List<Categories> findAll();
}
