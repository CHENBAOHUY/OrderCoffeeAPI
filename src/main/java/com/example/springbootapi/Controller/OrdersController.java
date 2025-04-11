package com.example.springbootapi.Controller;

import com.example.springbootapi.Entity.Orders;
import com.example.springbootapi.Entity.OrderDetails;
import com.example.springbootapi.Entity.Payments;
import com.example.springbootapi.Entity.Users;
import com.example.springbootapi.Service.OrdersService;
import com.example.springbootapi.Service.OrderDetailsService;
import com.example.springbootapi.Service.VNPayService;
import com.example.springbootapi.dto.OrderDetailResponse;
import com.example.springbootapi.dto.OrderResponse;
import jakarta.validation.Valid;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailsService orderDetailsService;

    @Autowired
    private VNPayService vnPayService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllOrders() {
        List<Orders> orders = ordersService.getAllOrders();
        if (orders.isEmpty()) {
            return ResponseEntity.status(404).body(createErrorResponse("No orders found!"));
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

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Integer id) {
        Optional<Orders> order = ordersService.getOrderById(id);
        if (order.isPresent()) {
            OrderResponse response = new OrderResponse(order.get());
            List<OrderDetailResponse> details = orderDetailsService.getOrderDetailsByOrderId(order.get().getId())
                    .stream()
                    .map(OrderDetailResponse::new)
                    .toList();
            response.setOrderDetails(details);
            return ResponseEntity.ok(response);
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
            String paymentMethod = orderRequest.getPaymentMethod();

            OrderResponse newOrder = ordersService.createOrder(order, orderDetails, paymentMethod);

            if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
                try {
                    String paymentUrl = vnPayService.createPaymentUrl(newOrder);
                    Map<String, Object> response = new HashMap<>();
                    response.put("order", newOrder);
                    response.put("paymentUrl", paymentUrl);
                    return ResponseEntity.status(201).body(response);
                } catch (UnsupportedEncodingException e) {
                    return ResponseEntity.status(500).body(createErrorResponse("Failed to generate VNPay payment URL: " + e.getMessage()));
                }
            } else {
                return ResponseEntity.status(201).body(newOrder);
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Integer id, @RequestBody Map<String, String> status) {
        try {
            OrderResponse updatedOrder = ordersService.updateOrderStatus(id, status.get("status"));
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

    @GetMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestParam Map<String, String> params) throws UnsupportedEncodingException {
        String vnp_TxnRef = params.get("vnp_TxnRef");
        String vnp_ResponseCode = params.get("vnp_ResponseCode");
        String vnp_SecureHash = params.get("vnp_SecureHash");

        Map<String, String> fields = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!entry.getKey().equals("vnp_SecureHash")) {
                fields.put(entry.getKey(), entry.getValue());
            }
        }
        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            hashData.append(entry.getKey()).append("=")
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)).append("&");
        }
        hashData.deleteCharAt(hashData.length() - 1);
        String calculatedHash = hmacSHA512("JX6AYQK65LZX4AED6OI7E58S0RB885OE", hashData.toString());

        if (!calculatedHash.equals(vnp_SecureHash)) {
            return ResponseEntity.status(400).body("Invalid signature");
        }

        Orders order = ordersService.getOrderById(Integer.parseInt(vnp_TxnRef))
                .orElseThrow(() -> new RuntimeException("Order not found"));
        Payments payment = order.getPayments().get(0);
        if ("00".equals(vnp_ResponseCode)) {
            order.setStatus(Orders.OrderStatus.COMPLETED);
            payment.setStatus(Payments.PaymentStatus.COMPLETED);
            payment.setTransactionId(params.get("vnp_TransactionNo"));
            ordersService.updateOrder(order);
            return ResponseEntity.ok("Payment successful");
        } else {
            order.setStatus(Orders.OrderStatus.CANCELLED);
            payment.setStatus(Payments.PaymentStatus.FAILED);
            ordersService.updateOrder(order);
            return ResponseEntity.status(400).body("Payment failed");
        }
    }

    private String hmacSHA512(String key, String data) throws RuntimeException {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC SHA512", e);
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

class OrderRequest {
    private Users user;
    private List<OrderDetails> orderDetails;
    private String paymentMethod;

    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }
    public List<OrderDetails> getOrderDetails() { return orderDetails; }
    public void setOrderDetails(List<OrderDetails> orderDetails) { this.orderDetails = orderDetails; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}