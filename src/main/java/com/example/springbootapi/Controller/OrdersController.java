package com.example.springbootapi.Controller;

import com.example.springbootapi.Entity.Orders;
import com.example.springbootapi.Entity.OrderDetails;
import com.example.springbootapi.Entity.Payments;
import com.example.springbootapi.Entity.Users;
import com.example.springbootapi.Service.OrdersService;
import com.example.springbootapi.Service.OrderDetailsService;
import com.example.springbootapi.Service.PaymentsService;
import com.example.springbootapi.Service.VNPayService;
import com.example.springbootapi.dto.*;
import com.example.springbootapi.repository.OrdersRepository;
import com.example.springbootapi.repository.PaymentsRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {
    private static final Logger logger = LoggerFactory.getLogger(OrdersController.class);

    @Autowired
    private PaymentsService paymentsService;

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailsService orderDetailsService;

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private PaymentsRepository paymentsRepository;

    @Value("${vnpay.returnUrl}")
    private String baseUrl;

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

    @GetMapping("/success")
    public ResponseEntity<?> handleSuccessRedirect(@RequestParam(value = "orderId", required = false) Long orderId) {
        if (orderId == null) {
            return ResponseEntity.badRequest().body(createErrorResponse("orderId is required"));
        }
        logger.info("✅ Xử lý yêu cầu /api/orders/success cho orderId: {}", orderId);

        // Tìm đơn hàng
        Optional<Orders> orderOpt = ordersService.getOrderById(orderId.intValue());
        if (!orderOpt.isPresent()) {
            return ResponseEntity.status(404).body(createErrorResponse("Order not found with ID: " + orderId));
        }
        Orders order = orderOpt.get();
        Users user = order.getUser();

        // Tìm giao dịch thanh toán thành công
        Optional<Payments> paymentOpt = paymentsRepository.findByOrderIdAndStatus(orderId.intValue(), Payments.PaymentStatus.COMPLETED);
        if (!paymentOpt.isPresent()) {
            return ResponseEntity.status(404).body(createErrorResponse("Payment not found for order ID: " + orderId));
        }
        Payments payment = paymentOpt.get();

        // Tạo response theo cấu trúc mong muốn
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Thanh toán thành công cho đơn hàng #" + orderId);
        response.put("orderId", orderId);
        response.put("totalPrice", order.getTotalPrice());
        response.put("orderDate", order.getOrderDate().toString());
        response.put("paymentMethod", payment.getPaymentMethod());
        response.put("paymentDate", payment.getPaymentDate().toString());
        response.put("transactionId", payment.getTransactionId());
        response.put("userName", user.getName());
        response.put("userEmail", user.getEmail());

        // Danh sách sản phẩm
        List<Map<String, Object>> orderDetails = order.getOrderDetails().stream().map(detail -> {
            Map<String, Object> item = new HashMap<>();
            item.put("productName", detail.getProduct().getName());
            item.put("size", detail.getSize());
            item.put("quantity", detail.getQuantity());
            item.put("unitPrice", detail.getUnitPrice());
            item.put("itemTotalPrice", detail.getItemTotalPrice());
            return item;
        }).collect(Collectors.toList());
        response.put("orderDetails", orderDetails);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/callback/success")
    public ResponseEntity<?> handleCallbackSuccess(@RequestParam(value = "orderId", required = false) Long orderId) {
        if (orderId == null) {
            return ResponseEntity.badRequest().body(createErrorResponse("orderId is required"));
        }
        logger.info("✅ Xử lý yêu cầu /api/orders/callback/success cho orderId: {}", orderId);

        // Tìm đơn hàng
        Optional<Orders> orderOpt = ordersService.getOrderById(orderId.intValue());
        if (!orderOpt.isPresent()) {
            return ResponseEntity.status(404).body(createErrorResponse("Order not found with ID: " + orderId));
        }
        Orders order = orderOpt.get();
        Users user = order.getUser();

        // Tìm giao dịch thanh toán thành công
        Optional<Payments> paymentOpt = paymentsRepository.findByOrderIdAndStatus(orderId.intValue(), Payments.PaymentStatus.COMPLETED);
        if (!paymentOpt.isPresent()) {
            return ResponseEntity.status(404).body(createErrorResponse("Payment not found for order ID: " + orderId));
        }
        Payments payment = paymentOpt.get();

        // Tạo response theo cấu trúc mong muốn
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Thanh toán thành công cho đơn hàng #" + orderId);
        response.put("orderId", orderId);
        response.put("totalPrice", order.getTotalPrice());
        response.put("orderDate", order.getOrderDate().toString());
        response.put("paymentMethod", payment.getPaymentMethod());
        response.put("paymentDate", payment.getPaymentDate().toString());
        response.put("transactionId", payment.getTransactionId());
        response.put("userName", user.getName());
        response.put("userEmail", user.getEmail());

        // Danh sách sản phẩm
        List<Map<String, Object>> orderDetails = order.getOrderDetails().stream().map(detail -> {
            Map<String, Object> item = new HashMap<>();
            item.put("productName", detail.getProduct().getName());
            item.put("size", detail.getSize());
            item.put("quantity", detail.getQuantity());
            item.put("unitPrice", detail.getUnitPrice());
            item.put("itemTotalPrice", detail.getItemTotalPrice());
            return item;
        }).collect(Collectors.toList());
        response.put("orderDetails", orderDetails);

        return ResponseEntity.ok(response);
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
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequest orderRequest, HttpServletRequest request) {
        try {
            if (orderRequest.getOrderDetails() == null || orderRequest.getOrderDetails().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Order details cannot be null or empty"));
            }
            if (orderRequest.getUser() == null || orderRequest.getUser().getId() == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("User cannot be null and must have a valid ID"));
            }
            if (orderRequest.getPaymentMethod() == null || orderRequest.getPaymentMethod().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Payment method cannot be null or empty"));
            }

            for (OrderDetails detail : orderRequest.getOrderDetails()) {
                if (detail.getProduct() == null) {
                    return ResponseEntity.badRequest().body(createErrorResponse("Product in order details cannot be null"));
                }
                if (detail.getQuantity() == null || detail.getQuantity() <= 0) {
                    return ResponseEntity.badRequest().body(createErrorResponse("Quantity in order details must be greater than 0"));
                }
            }

            Orders order = new Orders();
            order.setUser(orderRequest.getUser());
            List<OrderDetails> orderDetails = orderRequest.getOrderDetails();
            String paymentMethod = orderRequest.getPaymentMethod();

            OrderResponse newOrder = ordersService.createOrder(order, orderDetails, paymentMethod);

            if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
                try {
                    // Kiểm tra xem đơn hàng引擎 đã có giao dịch thanh toán thành công chưa
                    List<Payments> existingPayments = paymentsRepository.findByOrderIdOrderByCreatedAtDesc(newOrder.getId());
                    if (existingPayments.stream().anyMatch(p -> p.getStatus() == Payments.PaymentStatus.COMPLETED)) {
                        return ResponseEntity.badRequest().body(createErrorResponse("Đơn hàng này đã được thanh toán"));
                    }

                    // Xóa các bản ghi PENDING trước đó (nếu có) để tránh dư thừa
                    List<Payments> pendingPayments = existingPayments.stream()
                            .filter(p -> p.getStatus() == Payments.PaymentStatus.PENDING)
                            .collect(Collectors.toList());
                    for (Payments pending : pendingPayments) {
                        paymentsRepository.delete(pending);
                        logger.info("Deleted previous PENDING payment with ID {} for order {}", pending.getId(), newOrder.getId());
                    }

                    // Tạo vnp_TxnRef duy nhất
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                    String vnp_TxnRef = newOrder.getId() + "_" + timestamp;

                    // Lưu giao dịch vào Payments
                    PaymentsDTO paymentDTO = paymentsService.createPayment(newOrder.getId(), newOrder.getTotalPrice(), paymentMethod, vnp_TxnRef);

                    // Tạo payment URL
                    String paymentUrl = vnPayService.createPaymentUrl(newOrder, request, vnp_TxnRef);
                    Map<String, Object> response = new HashMap<>();
                    response.put("order", newOrder);
                    response.put("paymentUrl", paymentUrl);
                    response.put("txnRef", vnp_TxnRef);
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
    @Transactional
    public ResponseEntity<?> handleCallback(HttpServletRequest request, @RequestParam Map<String, String> params) {
        try {
            logger.info("📥 Nhận callback từ VNPay - HTTP Method: {}", request.getMethod());

            String queryString = request.getQueryString();
            logger.info("📥 Nhận callback từ VNPay - Query String: {}", queryString != null ? queryString : "null");

            logger.info("📥 Nhận callback từ VNPay - Params: {}", params);

            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            } catch (IOException e) {
                logger.error("⚠️ Không thể đọc body: {}", e.getMessage());
            }
            logger.info("📥 Nhận callback từ VNPay - Body: {}", body.length() > 0 ? body.toString() : "null");

            Map<String, String> finalParams = new HashMap<>(params);

            if (finalParams.isEmpty() && body.length() > 0) {
                logger.info("🔍 Thử lấy tham số từ body...");
                String[] bodyParams = body.toString().split("&");
                for (String param : bodyParams) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        finalParams.put(keyValue[0], keyValue[1]);
                    }
                }
            }

            if (finalParams.isEmpty()) {
                logger.error("❌ Không nhận được tham số nào từ VNPay");
                return ResponseEntity.badRequest().body(createErrorResponse("Không nhận được tham số từ VNPay"));
            }

            if (!vnPayService.verifyCallback(finalParams, vnPayService.getHashSecret())) {
                logger.error("❌ Chữ ký không hợp lệ - Yêu cầu không đến từ VNPay");
                return ResponseEntity.badRequest().body(createErrorResponse("Chữ ký không hợp lệ"));
            }
            logger.info("✅ Chữ ký hợp lệ - Yêu cầu từ VNPay được xác nhận");

            VnPayCallbackDTO callbackData = new VnPayCallbackDTO();
            callbackData.setVnp_TxnRef(finalParams.get("vnp_TxnRef"));
            callbackData.setVnp_Amount(finalParams.get("vnp_Amount"));
            callbackData.setVnp_ResponseCode(finalParams.get("vnp_ResponseCode"));
            callbackData.setVnp_TransactionNo(finalParams.get("vnp_TransactionNo"));
            callbackData.setVnp_BankCode(finalParams.get("vnp_BankCode"));
            callbackData.setVnp_CardType(finalParams.get("vnp_CardType"));
            callbackData.setVnp_PayDate(finalParams.get("vnp_PayDate"));
            callbackData.setVnp_OrderInfo(finalParams.get("vnp_OrderInfo"));

            logger.info("🔍 Kiểm tra tham số: vnp_TxnRef={}, vnp_Amount={}, vnp_ResponseCode={}",
                    callbackData.getVnp_TxnRef(), callbackData.getVnp_Amount(), callbackData.getVnp_ResponseCode());

            PaymentResultDTO result = paymentsService.processVnPayCallback(callbackData);
            logger.info("✅ Kết quả xử lý callback: success={}, message={}", result.isSuccess(), result.getMessage());

            if (result.isSuccess()) {
                Integer orderId = result.getOrderId();
                Orders order = ordersRepository.findById(orderId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));
                Users user = order.getUser();

                logger.info("👤 Callback thuộc về người dùng: ID={}, Name={}, Email={}",
                        user.getId(), user.getName(), user.getEmail());

                String successUrl = baseUrl + "/callback/success?orderId=" + result.getOrderId();
                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "Thanh toán thành công");
                response.put("orderId", result.getOrderId());
                response.put("transactionId", result.getTransactionId());
                response.put("successUrl", successUrl);
                response.put("userId", user.getId());
                response.put("userName", user.getName());
                response.put("userEmail", user.getEmail());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse(result.getMessage()));
            }
        } catch (Exception e) {
            logger.error("❌ Lỗi xử lý callback: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi xử lý callback: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<?> getOrderDetails(@PathVariable Integer id) {
        try {
            Optional<Orders> orderOpt = ordersService.getOrderById(id);
            if (!orderOpt.isPresent()) {
                return ResponseEntity.status(404).body(createErrorResponse("Order not found with ID: " + id));
            }

            Orders order = orderOpt.get();
            List<OrderDetails> orderDetails = orderDetailsService.getOrderDetailsByOrderId(id);
            List<Payments> payments = paymentsService.getPaymentsByOrderId(id).stream()
                    .map(dto -> {
                        Payments p = new Payments();
                        p.setId(dto.getId());
                        p.setOrder(order);
                        p.setPaymentMethod(dto.getPaymentMethod());
                        p.setStatus(Payments.PaymentStatus.valueOf(dto.getPaymentStatus()));
                        p.setAmount(dto.getAmount());
                        p.setPaymentDate(dto.getPaymentDate());
                        p.setTransactionId(dto.getTransactionId());
                        p.setResponseCode(dto.getResponseCode());
                        p.setResponseMessage(dto.getResponseMessage());
                        p.setBankCode(dto.getBankCode());
                        p.setCardType(dto.getCardType());
                        return p;
                    })
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.getId());
            response.put("userId", order.getUser().getId());
            response.put("totalPrice", order.getTotalPrice());
            response.put("status", order.getStatus().name());
            response.put("orderDate", order.getOrderDate());
            response.put("orderDetails", orderDetails.stream().map(detail -> new HashMap<String, Object>() {{
                put("productId", detail.getProduct().getId());
                put("productName", detail.getProduct().getName());
                put("quantity", detail.getQuantity());
                put("unitPrice", detail.getUnitPrice());
                put("itemTotalPrice", detail.getItemTotalPrice());
                put("size", detail.getSize());
            }}).toList());
            response.put("payments", payments.stream().map(payment -> new HashMap<String, Object>() {{
                put("paymentId", payment.getId());
                put("paymentMethod", payment.getPaymentMethod());
                put("status", payment.getStatus().name());
                put("amount", payment.getAmount());
                put("paymentDate", payment.getPaymentDate());
                put("transactionId", payment.getTransactionId());
                put("responseCode", payment.getResponseCode());
                put("responseMessage", payment.getResponseMessage());
                put("bankCode", payment.getBankCode());
                put("cardType", payment.getCardType());
            }}).toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi lấy chi tiết đơn hàng: " + e.getMessage()));
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