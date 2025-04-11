package com.example.springbootapi.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal; // Thêm import
import java.time.LocalDateTime;

@Entity
@Table(name = "Payments")
@Getter
@Setter
public class Payments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Orders order;

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 50, columnDefinition = "NVARCHAR(50) DEFAULT 'Pending'")
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "payment_date", columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime paymentDate = LocalDateTime.now();

    @Column(name = "amount", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private BigDecimal amount; // Sửa từ Double thành BigDecimal

    @Column(name = "created_at", updatable = false, columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}