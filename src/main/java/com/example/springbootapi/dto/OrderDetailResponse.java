package com.example.springbootapi.dto;

import com.example.springbootapi.Entity.OrderDetails;

public class OrderDetailResponse {
    private Integer id;
    private Integer productId;
    private String productName;
    private Integer quantity;
    private Double itemTotalPrice;

    // Constructor
    public OrderDetailResponse(OrderDetails detail) {
        this.id = detail.getId();
        this.productId = detail.getProduct().getId();
        this.productName = detail.getProduct().getName();
        this.quantity = detail.getQuantity();
        this.itemTotalPrice = detail.getItemTotalPrice();
    }

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getItemTotalPrice() { return itemTotalPrice; }
    public void setItemTotalPrice(Double itemTotalPrice) { this.itemTotalPrice = itemTotalPrice; }
}