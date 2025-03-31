package com.example.springbootapi.dto;

import com.example.springbootapi.Entity.Orders;

import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
    private Integer id;
    private Integer userId;
    private String userName;
    private Double totalPrice;
    private String status;
    private LocalDateTime orderDate;
    private List<OrderDetailResponse> orderDetails;

    // Constructor
    public OrderResponse(Orders order) {
        this.id = order.getId();
        this.userId = order.getUser().getId();
        this.userName = order.getUser().getName();
        this.totalPrice = order.getTotalPrice();
        this.status = order.getStatus().name();
        this.orderDate = order.getOrderDate();
    }

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public List<OrderDetailResponse> getOrderDetails() { return orderDetails; }
    public void setOrderDetails(List<OrderDetailResponse> orderDetails) { this.orderDetails = orderDetails; }
}