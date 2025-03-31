package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.Orders;
import com.example.springbootapi.Entity.OrderDetails;
import com.example.springbootapi.Entity.Products;
import com.example.springbootapi.repository.OrdersRepository;
import com.example.springbootapi.repository.OrderDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrdersService {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private ProductsService productsService;

    @Transactional
    public Orders createOrder(Orders order, List<OrderDetails> orderDetails) {
        // Validate and set total price
        double totalPrice = orderDetails.stream()
                .mapToDouble(detail -> {
                    Products product = productsService.getProductById(detail.getProduct().getId())
                            .orElseThrow(() -> new RuntimeException("Product not found"));
                    detail.setItemTotalPrice(product.getPrice() * detail.getQuantity());
                    detail.setOrder(order);
                    return detail.getItemTotalPrice();
                })
                .sum();
        order.setTotalPrice(totalPrice);

        // Save order and details
        Orders savedOrder = ordersRepository.save(order);
        orderDetails.forEach(detail -> detail.setOrder(savedOrder));
        orderDetailsRepository.saveAll(orderDetails);

        return savedOrder;
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
    public Orders updateOrderStatus(Integer id, String status) {
        Orders order = ordersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(Orders.OrderStatus.valueOf(status.toUpperCase()));
        return ordersRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Integer id) {
        Orders order = ordersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        ordersRepository.delete(order);
    }
}