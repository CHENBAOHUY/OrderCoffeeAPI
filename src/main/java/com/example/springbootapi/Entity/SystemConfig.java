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
    private Integer id;

    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    private BigDecimal fromValuePrice;

    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    private BigDecimal toValuePoint;

    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime fromDate = LocalDateTime.now();

    private LocalDateTime thruDate;

    @ManyToOne
    @JoinColumn(name = "currency_id", nullable = false)
    private Currencies currency;
}
