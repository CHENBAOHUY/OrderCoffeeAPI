package com.example.springbootapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatisticsDTO {
    private Integer productId;
    private String productName;
    private Double price;
    private Integer quantitySold;
    private Double revenue;
    private String imageUrl;
}
