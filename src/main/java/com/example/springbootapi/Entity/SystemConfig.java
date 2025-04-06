package com.example.springbootapi.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "SystemConfig")
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "from_value_price", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private BigDecimal fromValuePrice;

    @Column(name = "to_value_point", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private BigDecimal toValuePoint;

    @Column(name = "from_date", nullable = false)
    private LocalDateTime fromDate = LocalDateTime.now();

    @Column(name = "thru_date")
    private LocalDateTime thruDate;

    @ManyToOne
    @JoinColumn(name = "currency_id", nullable = false)
    private Currencies currency;
}