package com.example.springbootapi.dto;

import com.example.springbootapi.Entity.OrderDetails;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderDetailResponse {
    private Integer id;
    private Integer productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal itemTotalPrice;
    private String size;

    public OrderDetailResponse(OrderDetails detail) {
        this.id = detail.getId();
        this.productId = detail.getProduct().getId();
        this.productName = detail.getProduct().getName();
        this.quantity = detail.getQuantity();
        this.unitPrice = detail.getUnitPrice();
        this.itemTotalPrice = detail.getItemTotalPrice();
        this.size = detail.getSize();
    }
}