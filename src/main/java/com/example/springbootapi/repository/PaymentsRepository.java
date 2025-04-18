package com.example.springbootapi.repository;

import com.example.springbootapi.Entity.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentsRepository extends JpaRepository<Payments, Integer> {

    // Tìm theo orderId
    @Query("SELECT p FROM Payments p WHERE p.order.id = :orderId")
    List<Payments> findByOrderId(Integer orderId);

    // Tìm theo orderId và sắp xếp theo thời gian tạo giảm dần
    @Query("SELECT p FROM Payments p WHERE p.order.id = :orderId ORDER BY p.createdAt DESC")
    List<Payments> findByOrderIdOrderByCreatedAtDesc(Integer orderId);

    // Thêm phương thức tìm theo orderId và status
    @Query("SELECT p FROM Payments p WHERE p.order.id = :orderId AND p.status = :status")
    Optional<Payments> findByOrderIdAndStatus(@Param("orderId") Integer orderId, @Param("status") Payments.PaymentStatus status);

    // Thêm phương thức tìm theo txnRef
    @Query("SELECT p FROM Payments p WHERE p.txnRef = :txnRef")
    Optional<Payments> findByTxnRef(@Param("txnRef") String txnRef);
}