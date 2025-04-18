package com.example.springbootapi.Controller;

import com.example.springbootapi.Service.StatisticsService;
import com.example.springbootapi.dto.DashboardStatisticsDTO;
import com.example.springbootapi.dto.ProductStatisticsDTO;
import com.example.springbootapi.dto.StatisticsRequestDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @PostMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardStatisticsDTO> getDashboardStats() {
        DashboardStatisticsDTO stats = statisticsService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/revenue/daily")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDailyRevenue(
            @Valid @RequestBody StatisticsRequestDTO request) {
        if (request.getDate() == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Ngày không được để trống");
            return ResponseEntity.badRequest().body(error);
        }

        Map<String, Object> result = statisticsService.getDailyRevenue(request.getDate());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/revenue/weekly")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getWeeklyRevenue(
            @Valid @RequestBody StatisticsRequestDTO request) {
        if (request.getStartDate() == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Ngày bắt đầu không được để trống");
            return ResponseEntity.badRequest().body(error);
        }

        Map<String, Object> result = statisticsService.getWeeklyRevenue(request.getStartDate());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/revenue/monthly")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getMonthlyRevenue(
            @Valid @RequestBody StatisticsRequestDTO request) {
        if (request.getYear() == null || request.getMonth() == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Năm và tháng không được để trống");
            return ResponseEntity.badRequest().body(error);
        }

        Map<String, Object> result = statisticsService.getMonthlyRevenue(request.getYear(), request.getMonth());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/revenue/yearly")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getYearlyRevenue(
            @Valid @RequestBody StatisticsRequestDTO request) {
        if (request.getYear() == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Năm không được để trống");
            return ResponseEntity.badRequest().body(error);
        }

        Map<String, Object> result = statisticsService.getYearlyRevenue(request.getYear());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/revenue/yearly/async")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getYearlyRevenueAsync(
            @Valid @RequestBody StatisticsRequestDTO request) {
        if (request.getYear() == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Năm không được để trống");
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(error));
        }

        return statisticsService.getYearlyRevenueAsync(request.getYear())
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/products/top-selling")
    // Không cần PreAuthorize vì đây là API công khai cho người dùng
    public ResponseEntity<List<ProductStatisticsDTO>> getTopSellingProducts(
            @Valid @RequestBody StatisticsRequestDTO request) {
        if (request.getStartDateTime() == null || request.getEndDateTime() == null) {
            return ResponseEntity.badRequest().build();
        }

        Integer limit = request.getLimit() != null ? request.getLimit() : 10;
        List<ProductStatisticsDTO> products = statisticsService.getTopSellingProducts(
                request.getStartDateTime(),
                request.getEndDateTime(),
                limit);

        return ResponseEntity.ok(products);
    }

    @PostMapping("/customers/top-spending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getTopCustomersBySpending(
            @Valid @RequestBody StatisticsRequestDTO request) {
        if (request.getStartDateTime() == null || request.getEndDateTime() == null) {
            return ResponseEntity.badRequest().build();
        }

        Integer limit = request.getLimit() != null ? request.getLimit() : 10;
        List<Map<String, Object>> customers = statisticsService.getTopCustomersBySpending(
                request.getStartDateTime(),
                request.getEndDateTime(),
                limit);

        return ResponseEntity.ok(customers);
    }

    @PostMapping("/revenue/by-category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getRevenueByCategory(
            @Valid @RequestBody StatisticsRequestDTO request) {
        if (request.getStartDateTime() == null || request.getEndDateTime() == null) {
            return ResponseEntity.badRequest().build();
        }

        List<Map<String, Object>> revenueByCategory = statisticsService.getRevenueByCategory(
                request.getStartDateTime(),
                request.getEndDateTime());

        return ResponseEntity.ok(revenueByCategory);
    }

    @PostMapping("/orders/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getOrdersCount(
            @Valid @RequestBody StatisticsRequestDTO request) {
        if (request.getStartDateTime() == null || request.getEndDateTime() == null) {
            return ResponseEntity.badRequest().build();
        }

        Map<String, Object> ordersCount = statisticsService.getOrdersCount(
                request.getStartDateTime(),
                request.getEndDateTime());

        return ResponseEntity.ok(ordersCount);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", e.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }
}