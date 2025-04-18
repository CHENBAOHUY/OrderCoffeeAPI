package com.example.springbootapi.dto;

import com.example.springbootapi.Entity.Payments;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentsDTO {
    private Integer id;
    private Integer orderId;
    private String paymentMethod;
    private BigDecimal amount;
    private String paymentStatus;
    private LocalDateTime paymentDate;
    private String transactionId;
    private String responseCode;
    private String responseMessage;
    private String bankCode;
    private String cardType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor cơ bản hiện tại
    public PaymentsDTO(Integer id, Integer orderId, String paymentMethod,
                       BigDecimal amount, String paymentStatus, LocalDateTime paymentDate) {
        this.id = id;
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.paymentDate = paymentDate;
    }

    // Constructor đầy đủ từ Entity
    public PaymentsDTO(Payments payment) {
        this.id = payment.getId();
        this.orderId = payment.getOrder() != null ? payment.getOrder().getId() : null;
        this.paymentMethod = payment.getPaymentMethod();
        this.amount = payment.getAmount();
        this.paymentStatus = payment.getStatus() != null ? payment.getStatus().name() : null;
        this.paymentDate = payment.getPaymentDate();
        this.transactionId = payment.getTransactionId();
        this.responseCode = payment.getResponseCode();
        this.responseMessage = payment.getResponseMessage();
        this.bankCode = payment.getBankCode();
        this.cardType = payment.getCardType();
        this.createdAt = payment.getCreatedAt();
        this.updatedAt = payment.getUpdatedAt();
    }
}