package com.example.springbootapi.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SystemConfigDTO {
    private Integer id;
    private BigDecimal fromValuePrice;
    private BigDecimal toValuePoint;
    private LocalDateTime fromDate;
    private LocalDateTime thruDate;
    private Integer currencyId;

}
