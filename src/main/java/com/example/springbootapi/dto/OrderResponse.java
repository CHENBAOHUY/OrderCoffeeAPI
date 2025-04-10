package com.example.springbootapi.dto;

import com.example.springbootapi.Entity.Orders;
import com.example.springbootapi.Entity.Payments;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Data
public class OrderResponse {
    private Integer id;
    private Integer userId;
    private String userName;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime orderDate;
    private List<OrderDetailResponse> orderDetails;
    private List<PaymentsDTO> payments;

    public OrderResponse(Orders order) {
        this.id = order.getId();
        this.userId = order.getUser() != null ? order.getUser().getId() : null; // Thêm kiểm tra null
        this.userName = order.getUser() != null ? order.getUser().getName() : null; // Thêm kiểm tra null
        this.totalPrice = order.getTotalPrice();
        this.status = order.getStatus().name();
        this.orderDate = order.getOrderDate();
        this.payments = order.getPayments().stream()
                .map(payment -> new PaymentsDTO(
                        payment.getId(),
                        payment.getOrder().getId(),
                        payment.getPaymentMethod(),
                        order.getTotalPrice(), // Dùng totalPrice từ Orders thay vì amount từ Payments
                        payment.getStatus().name(), // Sửa từ getPaymentStatus() thành getStatus().name()
                        payment.getPaymentDate()))
                .collect(Collectors.toList());
    }
}
