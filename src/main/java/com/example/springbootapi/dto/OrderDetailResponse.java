package com.example.springbootapi.dto;

import com.example.springbootapi.Entity.OrderDetails;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class OrderDetailResponse {
    private Integer id;
    private Integer productId;
    private String productName;
    private Integer quantity;
    private BigDecimal itemTotalPrice;

    // Constructor
    public OrderDetailResponse(OrderDetails detail) {
        this.id = detail.getId();
        this.productId = detail.getProduct().getId();
        this.productName = detail.getProduct().getName();
        this.quantity = detail.getQuantity();
        this.itemTotalPrice = detail.getItemTotalPrice();
    }

}