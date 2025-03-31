package com.example.springbootapi.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class OrdersDTO {
    private Integer id;
    private Integer userId;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime orderDate;

}
