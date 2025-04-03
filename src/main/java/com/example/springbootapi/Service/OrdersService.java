package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.Orders;
import com.example.springbootapi.Entity.OrderDetails;
import com.example.springbootapi.Entity.Payments;
import com.example.springbootapi.Entity.Products;
import com.example.springbootapi.dto.OrderDetailResponse;
import com.example.springbootapi.dto.OrderResponse;
import com.example.springbootapi.repository.OrdersRepository;
import com.example.springbootapi.repository.OrderDetailsRepository;
import com.example.springbootapi.repository.PaymentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrdersService {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private ProductsService productsService;

    @Autowired
    private OrderDetailsService orderDetailsService;

    @Autowired
    private PaymentsRepository paymentsRepository; // Thêm repository cho Payments

    // Ánh xạ từ Orders sang OrderResponse
    private OrderResponse toOrderResponse(Orders order) {
        OrderResponse response = new OrderResponse(order);
        List<OrderDetailResponse> details = orderDetailsService.getOrderDetailsByOrderId(order.getId())
                .stream()
                .map(OrderDetailResponse::new)
                .toList();
        response.setOrderDetails(details);
        return response;
    }

    @Transactional
    public OrderResponse createOrder(Orders order, List<OrderDetails> orderDetails, String paymentMethod) {
        double totalPrice = orderDetails.stream()
                .mapToDouble(detail -> {
                    Products product = productsService.getProductById(detail.getProduct().getId())
                            .orElseThrow(() -> new RuntimeException("Product not found"));
                    detail.setItemTotalPrice(product.getPrice() * detail.getQuantity());
                    detail.setUnitPrice(product.getPrice()); // Thêm unitPrice
                    detail.setOrder(order);
                    return detail.getItemTotalPrice();
                })
                .sum();
        order.setTotalPrice(totalPrice);
        order.setOrderDetails(orderDetails);

        // Tạo bản ghi thanh toán
        Payments payment = new Payments();
        payment.setOrder(order);
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(totalPrice);
        payment.setStatus(Payments.PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDateTime.now());
        order.getPayments().add(payment);

        Orders savedOrder = ordersRepository.save(order);
        return toOrderResponse(savedOrder);
    }

    public List<Orders> getAllOrders() {
        return ordersRepository.findAll();
    }

    public Optional<Orders> getOrderById(Integer id) {
        return ordersRepository.findById(id);
    }

    public List<Orders> getOrdersByUserId(Integer userId) {
        return ordersRepository.findByUserId(userId);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Integer id, String status) {
        Orders order = ordersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(Orders.OrderStatus.valueOf(status.toUpperCase()));
        Orders updatedOrder = ordersRepository.save(order);
        return toOrderResponse(updatedOrder);
    }

    @Transactional
    public void deleteOrder(Integer id) {
        Orders order = ordersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        ordersRepository.delete(order);
    }
}