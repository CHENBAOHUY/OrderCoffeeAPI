package com.example.springbootapi.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class PriceHistoryDTO {
    private Integer id;
    private Integer productId;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private LocalDateTime changedAt;

}
