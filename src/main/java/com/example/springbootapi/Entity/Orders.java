package com.example.springbootapi.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
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
    @NotNull(message = "User is required")
    private Users user;

    @DecimalMin(value = "0.0", inclusive = false, message = "Total price must be greater than 0")
    private Double totalPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    private LocalDateTime orderDate = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetails> orderDetails;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payments> payments;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reviews> reviews;

    public enum OrderStatus {
        PENDING, COMPLETED, CANCELLED
    }
}