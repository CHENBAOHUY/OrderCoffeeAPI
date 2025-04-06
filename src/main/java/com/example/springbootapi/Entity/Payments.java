package com.example.springbootapi.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Payments")
public class Payments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = false, columnDefinition = "NVARCHAR(50) DEFAULT 'Pending'")
    private String paymentStatus;

    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime paymentDate = LocalDateTime.now();
}
