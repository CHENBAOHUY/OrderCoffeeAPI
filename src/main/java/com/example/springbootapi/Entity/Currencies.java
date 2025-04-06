package com.example.springbootapi.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Currencies")
public class Currencies {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "currency_code", nullable = false, unique = true, length = 10)
    private String currencyCode;

    @Column(name = "currency_name", nullable = false, length = 255)
    private String currencyName;
}