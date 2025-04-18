package com.example.springbootapi.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
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
    private BigDecimal amount;

    @Column(name = "created_at", updatable = false, columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    // Thêm các trường mới
    @Column(name = "response_code", length = 10)
    private String responseCode;

    @Column(name = "response_message", length = 255)
    private String responseMessage;

    @Column(name = "bank_code", length = 20)
    private String bankCode;

    @Column(name = "txn_ref", length = 100) // Thêm trường để lưu vnp_TxnRef
    private String txnRef;

    @Column(name = "card_type", length = 20)
    private String cardType;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}