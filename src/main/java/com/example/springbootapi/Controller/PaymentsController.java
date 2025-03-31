package com.example.springbootapi.Controller;

import com.example.springbootapi.Entity.Payments;
import com.example.springbootapi.Service.PaymentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
public class PaymentsController {

    @Autowired
    private PaymentsService paymentsService;

    @GetMapping
    public List<Payments> getAllPayments() {
        return paymentsService.getAllPayments();
    }

    @GetMapping("/{id}")
    public Optional<Payments> getPaymentById(@PathVariable Integer id) {
        return paymentsService.getPaymentById(id);
    }

    @PostMapping
    public Payments addPayment(@RequestBody Payments payment) {
        return paymentsService.addPayment(payment);
    }

    @DeleteMapping("/{id}")
    public void deletePayment(@PathVariable Integer id) {
        paymentsService.deletePayment(id);
    }
}
