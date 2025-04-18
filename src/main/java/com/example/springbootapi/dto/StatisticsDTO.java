package com.example.springbootapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDTO {
    private Double totalRevenue;
    private Long orderCount;
    private Double averageOrderValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Map<String, Object> details;
}
