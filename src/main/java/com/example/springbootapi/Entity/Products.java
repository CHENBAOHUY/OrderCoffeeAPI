package com.example.springbootapi.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "Products")
public class Products {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    private Double price;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties("products")
    private Categories categories;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String image;

    @Column(name = "is_deleted", columnDefinition = "BIT DEFAULT 0")
    private Boolean isDeleted;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "created_at", updatable = false, columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "active")
    private Boolean active;

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}