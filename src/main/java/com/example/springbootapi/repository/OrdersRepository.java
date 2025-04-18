// OrdersRepository.java
package com.example.springbootapi.repository;

import com.example.springbootapi.Entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Integer> {
    List<Orders> findByUserId(Integer userId);
    List<Orders> findByUserIdOrderByOrderDateDesc(Integer userId);
    // Thêm các phương thức thống kê
    @Query(value = "SELECT SUM(o.total_price) FROM orders o WHERE o.status = 'COMPLETED' AND o.order_date BETWEEN :startDate AND :endDate", nativeQuery = true)
    Double calculateTotalRevenueForDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT COUNT(o.id) FROM orders o WHERE o.status = 'COMPLETED' AND o.order_date BETWEEN :startDate AND :endDate", nativeQuery = true)
    Long countOrdersForDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT COUNT(o.id) FROM orders o WHERE o.status = :status AND o.order_date BETWEEN :startDate AND :endDate", nativeQuery = true)
    Long countOrdersByStatusForDateRange(@Param("status") String status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT p.id as productId, p.product_name as productName, p.price, SUM(oi.quantity) as quantitySold, " +
            "SUM(oi.quantity * p.price) as revenue, p.image_url as imageUrl " +
            "FROM orders o " +
            "JOIN order_items oi ON o.id = oi.order_id " +
            "JOIN products p ON oi.product_id = p.id " +
            "WHERE o.status = 'COMPLETED' AND o.order_date BETWEEN :startDate AND :endDate " +
            "GROUP BY p.id, p.product_name, p.price, p.image_url " +
            "ORDER BY revenue DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Map<String, Object>> findTopSellingProducts(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("limit") int limit);

    @Query(value = "SELECT u.id as userId, u.name as userName, u.email, COUNT(o.id) as orderCount, SUM(o.total_price) as totalSpent " +
            "FROM orders o " +
            "JOIN users u ON o.user_id = u.id " +
            "WHERE o.status = 'COMPLETED' AND o.order_date BETWEEN :startDate AND :endDate " +
            "GROUP BY u.id, u.name, u.email " +
            "ORDER BY totalSpent DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Map<String, Object>> findTopCustomersBySpending(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("limit") int limit);

    @Query(value = "SELECT c.id as categoryId, c.name as categoryName, COUNT(o.id) as orderCount, SUM(oi.quantity * p.price) as revenue " +
            "FROM orders o " +
            "JOIN order_items oi ON o.id = oi.order_id " +
            "JOIN products p ON oi.product_id = p.id " +
            "JOIN categories c ON p.category_id = c.id " +
            "WHERE o.status = 'COMPLETED' AND o.order_date BETWEEN :startDate AND :endDate " +
            "GROUP BY c.id, c.name " +
            "ORDER BY revenue DESC", nativeQuery = true)
    List<Map<String, Object>> findRevenueByCategory(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT DATE(o.order_date) as date, SUM(o.total_price) as revenue " +
            "FROM orders o " +
            "WHERE o.status = 'COMPLETED' AND o.order_date BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(o.order_date) " +
            "ORDER BY date", nativeQuery = true)
    List<Map<String, Object>> findDailyRevenueInRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

}