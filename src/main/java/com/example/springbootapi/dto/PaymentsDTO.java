package com.example.springbootapi.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentsDTO {
    private Integer id;
    private Integer orderId;
    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime paymentDate;

}
