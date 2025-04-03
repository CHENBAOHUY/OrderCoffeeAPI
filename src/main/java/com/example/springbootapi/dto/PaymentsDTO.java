package com.example.springbootapi.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentsDTO {
    private Integer id;
    private Integer orderId;
    private String paymentMethod;
    private Double amount;
    private String paymentStatus;
    private LocalDateTime paymentDate;

    // Constructor thủ công
    public PaymentsDTO(Integer id, Integer orderId, String paymentMethod, Double amount, String paymentStatus, LocalDateTime paymentDate) {
        this.id = id;
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.paymentDate = paymentDate;
    }
}