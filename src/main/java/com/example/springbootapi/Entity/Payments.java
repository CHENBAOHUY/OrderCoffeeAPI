package com.example.springbootapi.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Payments")
@Getter
@Setter
public class Payments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Orders order;

    @NotNull(message = "Payment method is required")
    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    @Column(name = "amount", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 50, columnDefinition = "NVARCHAR(50) DEFAULT 'Pending'")
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "payment_date", columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime paymentDate = LocalDateTime.now();

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

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