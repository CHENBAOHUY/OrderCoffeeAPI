package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.OrderDetails;
import com.example.springbootapi.repository.OrderDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderDetailsService {

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    public List<OrderDetails> getOrderDetailsByOrderId(Integer orderId) {
        return orderDetailsRepository.findByOrderId(orderId);
    }

    public Optional<OrderDetails> getOrderDetailById(Integer id) {
        return orderDetailsRepository.findById(id);
    }
}