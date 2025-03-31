package com.example.springbootapi.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Currencies")
public class Currencies {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String currencyCode;

    @Column(nullable = false)
    private String currencyName;
}
