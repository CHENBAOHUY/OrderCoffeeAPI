package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.Payments;
import com.example.springbootapi.repository.PaymentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentsService {
    private final PaymentsRepository paymentsRepository;

    @Autowired
    public PaymentsService(PaymentsRepository paymentsRepository) {
        this.paymentsRepository = paymentsRepository;
    }

    public List<Payments> getAllPayments() {
        return paymentsRepository.findAll();
    }

    public Optional<Payments> getPaymentById(Integer id) {
        return paymentsRepository.findById(id);
    }

    public Payments createPayment(Payments payment) {
        return paymentsRepository.save(payment);
    }
    public Payments addPayment(Payments payment) {
        return paymentsRepository.save(payment);
    }
    public void deletePayment(Integer id) {
        paymentsRepository.deleteById(id);
    }
}
