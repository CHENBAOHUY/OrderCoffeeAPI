package com.example.springbootapi.repository;

import com.example.springbootapi.Entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BannerRepository extends JpaRepository<Banner, Integer> {
    @Query("SELECT b FROM Banner b WHERE b.isActive = true AND b.isDeleted = false ORDER BY b.createdAt DESC")
    List<Banner> findActiveBanners();

    Optional<Banner> findByIdAndIsDeletedFalse(Integer id);
}