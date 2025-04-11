package com.example.springbootapi.repository;

import com.example.springbootapi.Entity.Currencies;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CurrenciesRepository extends JpaRepository<Currencies, Integer> {
    Optional<Currencies> findByCurrencyCode(String currencyCode);
}