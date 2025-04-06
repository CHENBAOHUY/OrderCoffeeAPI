package com.example.springbootapi.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "PriceHistory")
public class PriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Products product;

    @Column(name = "old_price", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private BigDecimal oldPrice;

    @Column(name = "new_price", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private BigDecimal newPrice;

    @Column(name = "changed_at", nullable = false, columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime changedAt = LocalDateTime.now();
}