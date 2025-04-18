package com.example.springbootapi.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class PaymentResultDTO {
    private boolean success;
    private Integer orderId;
    private String message;
    private String transactionId;
    private String paymentMethod;
    private String bankCode;
    private String cardType;
    private LocalDateTime paymentTime;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private List<OrderDetailsDTO> orderItems;
}