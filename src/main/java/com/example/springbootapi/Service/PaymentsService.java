package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.Payments;
import com.example.springbootapi.dto.PaymentsDTO;
import com.example.springbootapi.repository.PaymentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PaymentsService {

    @Autowired
    private PaymentsRepository paymentsRepository;

    public List<PaymentsDTO> getAllPayments() {
        return paymentsRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<PaymentsDTO> getPaymentById(Integer id) {
        return paymentsRepository.findById(id).map(this::toDTO);
    }

    public List<PaymentsDTO> getPaymentsByOrderId(Integer orderId) {
        return paymentsRepository.findByOrderId(orderId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentsDTO addPayment(Payments payment) {
        Payments savedPayment = paymentsRepository.save(payment);
        return toDTO(savedPayment);
    }

    @Transactional
    public void deletePayment(Integer id) {
        paymentsRepository.deleteById(id);
    }

    private PaymentsDTO toDTO(Payments payment) {
        return new PaymentsDTO(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getPaymentMethod(),
                payment.getOrder().getTotalPrice(), // Dùng totalPrice từ Orders
                payment.getStatus().name(),
                payment.getPaymentDate()
        );
    }
}