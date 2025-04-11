package com.example.springbootapi.repository;

import com.example.springbootapi.Entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SystemConfigRepository extends JpaRepository<SystemConfig, Integer> {
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.currency.currencyCode = :currencyCode " +
            "AND sc.fromDate <= :now AND (sc.thruDate IS NULL OR sc.thruDate >= :now)")
    Optional<SystemConfig> findActiveConfigByCurrencyCode(@Param("currencyCode") String currencyCode,
                                                          @Param("now") LocalDateTime now);
}