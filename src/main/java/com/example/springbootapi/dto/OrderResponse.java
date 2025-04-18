package com.example.springbootapi.dto;

import com.example.springbootapi.Entity.Orders;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderResponse {
    private Integer id;
    private Integer userId;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime orderDate;
    private List<OrderDetailResponse> orderDetails;

    public OrderResponse(Orders order) {
        this.id = order.getId();
        this.userId = order.getUser().getId();
        this.totalPrice = order.getTotalPrice();
        this.status = order.getStatus().name();
        this.orderDate = order.getOrderDate();
    }
}