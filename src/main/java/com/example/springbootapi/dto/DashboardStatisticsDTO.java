package com.example.springbootapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatisticsDTO {
    private Long totalProducts;
    private Long totalCustomers;
    private Long totalOrders;
    private Double totalRevenue;
    private List<Map<String, Object>> recentRevenue;
    private List<ProductStatisticsDTO> topProducts;
}
