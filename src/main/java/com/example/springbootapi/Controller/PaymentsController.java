package com.example.springbootapi.Controller;

import com.example.springbootapi.dto.PaymentsDTO;
import com.example.springbootapi.Entity.Payments;
import com.example.springbootapi.Service.PaymentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
public class PaymentsController {

    @Autowired
    private PaymentsService paymentsService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentsDTO>> getAllPayments() {
        List<PaymentsDTO> payments = paymentsService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    public ResponseEntity<PaymentsDTO> getPaymentById(@PathVariable Integer id) {
        Optional<PaymentsDTO> payment = paymentsService.getPaymentById(id);
        return payment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    public ResponseEntity<List<PaymentsDTO>> getPaymentsByOrderId(@PathVariable Integer orderId) {
        List<PaymentsDTO> payments = paymentsService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(payments);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentsDTO> addPayment(@RequestBody Payments payment) {
        PaymentsDTO savedPayment = paymentsService.addPayment(payment);
        return ResponseEntity.status(201).body(savedPayment);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePayment(@PathVariable Integer id) {
        paymentsService.deletePayment(id);
        return ResponseEntity.ok().build();
    }
}