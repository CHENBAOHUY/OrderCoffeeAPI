package com.example.springbootapi.Controller;

import com.example.springbootapi.Entity.Orders;
import com.example.springbootapi.Entity.OrderDetails;
import com.example.springbootapi.Entity.Users;
import com.example.springbootapi.Service.OrdersService;
import com.example.springbootapi.Service.OrderDetailsService;
import com.example.springbootapi.dto.OrderDetailResponse;
import com.example.springbootapi.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailsService orderDetailsService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllOrders() {
        List<Orders> orders = ordersService.getAllOrders();
        if (orders.isEmpty()) {
            return ResponseEntity.status(404).body(createErrorResponse("No orders found!"));
        }
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Integer id) {
        Optional<Orders> order = ordersService.getOrderById(id);
        if (order.isPresent()) {
            return ResponseEntity.ok(order.get());
        } else {
            return ResponseEntity.status(404).body(createErrorResponse("Order not found with ID: " + id));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUserId(@PathVariable Integer userId) {
        List<Orders> orders = ordersService.getOrdersByUserId(userId);
        if (orders.isEmpty()) {
            return ResponseEntity.status(404).body(createErrorResponse("No orders found for user ID: " + userId));
        }
        List<OrderResponse> orderResponses = orders.stream()
                .map(order -> {
                    OrderResponse response = new OrderResponse(order);
                    List<OrderDetailResponse> details = orderDetailsService.getOrderDetailsByOrderId(order.getId())
                            .stream()
                            .map(OrderDetailResponse::new)
                            .toList();
                    response.setOrderDetails(details);
                    return response;
                })
                .toList();
        return ResponseEntity.ok(orderResponses);
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        try {
            Orders order = new Orders();
            order.setUser(orderRequest.getUser());
            List<OrderDetails> orderDetails = orderRequest.getOrderDetails();
            Orders newOrder = ordersService.createOrder(order, orderDetails);
            return ResponseEntity.status(201).body(newOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Integer id, @RequestBody Map<String, String> status) {
        try {
            Orders updatedOrder = ordersService.updateOrderStatus(id, status.get("status"));
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteOrder(@PathVariable Integer id) {
        Optional<Orders> order = ordersService.getOrderById(id);
        if (order.isPresent()) {
            ordersService.deleteOrder(id);
            return ResponseEntity.ok(createSuccessResponse("Order ID: " + id + " deleted successfully."));
        } else {
            return ResponseEntity.status(404).body(createErrorResponse("Order not found with ID: " + id));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Error");
        response.put("message", message);
        return response;
    }

    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        return response;
    }
}

// DTO for Order Request
class OrderRequest {
    private Users user;
    private List<OrderDetails> orderDetails;

    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }
    public List<OrderDetails> getOrderDetails() { return orderDetails; }
    public void setOrderDetails(List<OrderDetails> orderDetails) { this.orderDetails = orderDetails; }
}