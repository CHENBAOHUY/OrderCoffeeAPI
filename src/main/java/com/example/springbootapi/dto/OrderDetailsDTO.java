package com.example.springbootapi.dto;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class OrderDetailsDTO {
    private Integer id;
    private Integer orderId;
    private Integer productId;
    private Integer quantity;
    private BigDecimal itemTotalPrice;

}
