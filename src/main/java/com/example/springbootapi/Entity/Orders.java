package com.example.springbootapi.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Orders")
@Getter
@Setter
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(name = "total_price", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private Double totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, columnDefinition = "NVARCHAR(50) DEFAULT 'Pending'")
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "order_date", columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime orderDate = LocalDateTime.now();

    @Column(name = "created_at", updatable = false, columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "shipping_address", length = 255)
    private String shippingAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetails> orderDetails = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payments> payments = new ArrayList<>();

    public enum OrderStatus {
        PENDING, COMPLETED, CANCELLED
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}