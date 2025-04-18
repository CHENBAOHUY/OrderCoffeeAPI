// StatisticsService.java
package com.example.springbootapi.Service;

import com.example.springbootapi.dto.DashboardStatisticsDTO;
import com.example.springbootapi.dto.ProductStatisticsDTO;
import com.example.springbootapi.dto.StatisticsDTO;
import com.example.springbootapi.repository.OrdersRepository;
import com.example.springbootapi.repository.ProductsRepository;
import com.example.springbootapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private UserRepository userRepository;

    public Map<String, Object> getDailyRevenue(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay().minusNanos(1);

        Double totalRevenue = ordersRepository.calculateTotalRevenueForDateRange(startOfDay, endOfDay);
        Long orderCount = ordersRepository.countOrdersForDateRange(startOfDay, endOfDay);

        // Xử lý trường hợp không có dữ liệu
        totalRevenue = totalRevenue != null ? totalRevenue : 0.0;
        orderCount = orderCount != null ? orderCount : 0L;
        Double averageOrderValue = orderCount > 0 ? totalRevenue / orderCount : 0.0;

        Map<String, Object> result = new HashMap<>();
        result.put("date", date.toString());
        result.put("totalRevenue", totalRevenue);
        result.put("orderCount", orderCount);
        result.put("averageOrderValue", averageOrderValue);
        return result;
    }

    public Map<String, Object> getWeeklyRevenue(LocalDate startOfWeek) {
        // Đảm bảo startOfWeek là ngày đầu tuần (thứ 2)
        if (startOfWeek.getDayOfWeek() != DayOfWeek.MONDAY) {
            startOfWeek = startOfWeek.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
        }

        LocalDate endOfWeek = startOfWeek.plusDays(6);
        LocalDateTime startDateTime = startOfWeek.atStartOfDay();
        LocalDateTime endDateTime = endOfWeek.plusDays(1).atStartOfDay().minusNanos(1);

        Double totalRevenue = ordersRepository.calculateTotalRevenueForDateRange(startDateTime, endDateTime);
        Long orderCount = ordersRepository.countOrdersForDateRange(startDateTime, endDateTime);

        totalRevenue = totalRevenue != null ? totalRevenue : 0.0;
        orderCount = orderCount != null ? orderCount : 0L;
        Double averageOrderValue = orderCount > 0 ? totalRevenue / orderCount : 0.0;

        // Get daily breakdown
        List<Map<String, Object>> dailyRevenue = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate currentDay = startOfWeek.plusDays(i);
            dailyRevenue.add(getDailyRevenue(currentDay));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("startDate", startOfWeek.toString());
        result.put("endDate", endOfWeek.toString());
        result.put("totalRevenue", totalRevenue);
        result.put("orderCount", orderCount);
        result.put("averageOrderValue", averageOrderValue);
        result.put("dailyBreakdown", dailyRevenue);
        return result;
    }

    public Map<String, Object> getMonthlyRevenue(int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth());
        LocalDateTime startDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endDateTime = endOfMonth.plusDays(1).atStartOfDay().minusNanos(1);

        Double totalRevenue = ordersRepository.calculateTotalRevenueForDateRange(startDateTime, endDateTime);
        Long orderCount = ordersRepository.countOrdersForDateRange(startDateTime, endDateTime);

        totalRevenue = totalRevenue != null ? totalRevenue : 0.0;
        orderCount = orderCount != null ? orderCount : 0L;
        Double averageOrderValue = orderCount > 0 ? totalRevenue / orderCount : 0.0;

        // Get weekly breakdown
        List<Map<String, Object>> weeklyRevenue = new ArrayList<>();
        LocalDate current = startOfMonth;
        while (current.getMonthValue() == month) {
            // Find the first Monday in this period
            LocalDate monday = current;
            if (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
                monday = monday.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
                // If this Monday is in the next month, break
                if (monday.getMonthValue() != month) break;
            }
            weeklyRevenue.add(getWeeklyRevenue(monday));
            current = monday.plusWeeks(1);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("year", year);
        result.put("month", month);
        result.put("totalRevenue", totalRevenue);
        result.put("orderCount", orderCount);
        result.put("averageOrderValue", averageOrderValue);
        result.put("weeklyBreakdown", weeklyRevenue);
        return result;
    }

    @Cacheable(value = "yearlyRevenue", key = "#year")
    public Map<String, Object> getYearlyRevenue(int year) {
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);
        LocalDateTime startDateTime = startOfYear.atStartOfDay();
        LocalDateTime endDateTime = endOfYear.plusDays(1).atStartOfDay().minusNanos(1);

        Double totalRevenue = ordersRepository.calculateTotalRevenueForDateRange(startDateTime, endDateTime);
        Long orderCount = ordersRepository.countOrdersForDateRange(startDateTime, endDateTime);

        totalRevenue = totalRevenue != null ? totalRevenue : 0.0;
        orderCount = orderCount != null ? orderCount : 0L;
        Double averageOrderValue = orderCount > 0 ? totalRevenue / orderCount : 0.0;

        // Get monthly breakdown
        List<Map<String, Object>> monthlyRevenue = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            monthlyRevenue.add(getMonthlyRevenue(year, month));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("year", year);
        result.put("totalRevenue", totalRevenue);
        result.put("orderCount", orderCount);
        result.put("averageOrderValue", averageOrderValue);
        result.put("monthlyBreakdown", monthlyRevenue);
        return result;
    }

    public List<ProductStatisticsDTO> getTopSellingProducts(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        List<Map<String, Object>> results = ordersRepository.findTopSellingProducts(startDate, endDate, limit);
        return results.stream().map(row -> {
            ProductStatisticsDTO dto = new ProductStatisticsDTO();
            dto.setProductId((Integer) row.get("productId"));
            dto.setProductName((String) row.get("productName"));
            dto.setPrice((Double) row.get("price"));
            dto.setQuantitySold(((Number) row.get("quantitySold")).intValue());
            dto.setRevenue((Double) row.get("revenue"));
            dto.setImageUrl((String) row.get("imageUrl"));
            return dto;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getTopCustomersBySpending(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        return ordersRepository.findTopCustomersBySpending(startDate, endDate, limit);
    }

    public List<Map<String, Object>> getRevenueByCategory(LocalDateTime startDate, LocalDateTime endDate) {
        return ordersRepository.findRevenueByCategory(startDate, endDate);
    }

    public Map<String, Object> getOrdersCount(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> result = new HashMap<>();

        Long completedOrders = ordersRepository.countOrdersByStatusForDateRange("COMPLETED", startDate, endDate);
        Long pendingOrders = ordersRepository.countOrdersByStatusForDateRange("PENDING", startDate, endDate);
        Long canceledOrders = ordersRepository.countOrdersByStatusForDateRange("CANCELED", startDate, endDate);
        Long processingOrders = ordersRepository.countOrdersByStatusForDateRange("PROCESSING", startDate, endDate);
        Long shippingOrders = ordersRepository.countOrdersByStatusForDateRange("SHIPPING", startDate, endDate);

        result.put("completed", completedOrders != null ? completedOrders : 0);
        result.put("pending", pendingOrders != null ? pendingOrders : 0);
        result.put("canceled", canceledOrders != null ? canceledOrders : 0);
        result.put("processing", processingOrders != null ? processingOrders : 0);
        result.put("shipping", shippingOrders != null ? shippingOrders : 0);
        result.put("total", (completedOrders != null ? completedOrders : 0) +
                (pendingOrders != null ? pendingOrders : 0) +
                (canceledOrders != null ? canceledOrders : 0) +
                (processingOrders != null ? processingOrders : 0) +
                (shippingOrders != null ? shippingOrders : 0));

        return result;
    }

    @Cacheable(value = "dashboardStats", key = "'dashboard'")
    public DashboardStatisticsDTO getDashboardStats() {
        DashboardStatisticsDTO dto = new DashboardStatisticsDTO();

        // Đếm tổng số sản phẩm
        dto.setTotalProducts(productsRepository.count());

        // Đếm số người dùng (khách hàng)
        dto.setTotalCustomers(userRepository.countByRole("CUSTOMER"));

        // Lấy số đơn hàng và doanh thu
        Long totalOrders = ordersRepository.count();
        Double totalRevenue = ordersRepository.calculateTotalRevenueForDateRange(
                LocalDateTime.of(2000, 1, 1, 0, 0),
                LocalDateTime.now());

        dto.setTotalOrders(totalOrders);
        dto.setTotalRevenue(totalRevenue != null ? totalRevenue : 0.0);

        // Lấy doanh thu 7 ngày gần đây
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6);
        dto.setRecentRevenue(ordersRepository.findDailyRevenueInRange(
                sevenDaysAgo.atStartOfDay(),
                today.plusDays(1).atStartOfDay().minusNanos(1)));

        // Lấy top 5 sản phẩm bán chạy trong tháng
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        dto.setTopProducts(getTopSellingProducts(
                firstDayOfMonth.atStartOfDay(),
                today.plusDays(1).atStartOfDay().minusNanos(1),
                5));

        return dto;
    }

    @Async("statisticsExecutor")
    public CompletableFuture<Map<String, Object>> getYearlyRevenueAsync(Integer year) {
        return CompletableFuture.completedFuture(getYearlyRevenue(year));
    }
}