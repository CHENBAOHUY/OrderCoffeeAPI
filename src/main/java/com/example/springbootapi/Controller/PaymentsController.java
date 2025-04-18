package com.example.springbootapi.Controller;

import com.example.springbootapi.Service.PaymentsService;
import com.example.springbootapi.dto.PaymentResultDTO;
import com.example.springbootapi.dto.PaymentsDTO;
import com.example.springbootapi.dto.VnPayCallbackDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
public class PaymentsController {

    @Autowired
    private PaymentsService paymentsService;

    @GetMapping
    public ResponseEntity<List<PaymentsDTO>> getAllPayments() {
        List<PaymentsDTO> payments = paymentsService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentsDTO> getPaymentById(@PathVariable Integer id) {
        Optional<PaymentsDTO> payment = paymentsService.getPaymentById(id);
        return payment.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentsDTO>> getPaymentsByOrderId(@PathVariable Integer orderId) {
        List<PaymentsDTO> payments = paymentsService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Endpoint xử lý callback từ VNPay
     */
    @PostMapping("/vnpay-callback")
    public ResponseEntity<PaymentResultDTO> processVnPayCallback(@RequestBody VnPayCallbackDTO callbackData) {
        PaymentResultDTO result = paymentsService.processVnPayCallback(callbackData);
        return ResponseEntity.ok(result);
    }

    /**
     * Endpoint public cho frontend gọi để lấy kết quả thanh toán
     */
    @GetMapping("/result/{orderId}")
    public ResponseEntity<PaymentResultDTO> getPaymentResult(@PathVariable Integer orderId) {
        PaymentResultDTO result = paymentsService.getPaymentResult(orderId);
        return ResponseEntity.ok(result);
    }
}