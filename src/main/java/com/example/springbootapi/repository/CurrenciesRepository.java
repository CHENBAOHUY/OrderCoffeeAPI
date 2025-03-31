package com.example.springbootapi.repository;

import com.example.springbootapi.Entity.Currencies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrenciesRepository extends JpaRepository<Currencies, Integer> {
}
