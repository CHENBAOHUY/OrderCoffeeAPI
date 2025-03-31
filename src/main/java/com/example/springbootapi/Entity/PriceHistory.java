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
    private Products product; // Đổi tên biến thành "product" để đúng nghĩa

    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    private BigDecimal oldPrice;

    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    private BigDecimal newPrice;

    @Column(nullable = false)
    private LocalDateTime changedAt = LocalDateTime.now(); // Đặt mặc định là thời gian hiện tại
}
