package com.example.springbootapi.Controller;

import com.example.springbootapi.Service.CartService;
import com.example.springbootapi.Service.PaymentsService; // Thêm import này
import com.example.springbootapi.Service.UsersService;
import com.example.springbootapi.Service.VNPayService;
import com.example.springbootapi.dto.*;
import com.example.springbootapi.Entity.Payments; // Thêm import này
import com.example.springbootapi.repository.PaymentsRepository; // Thêm import này
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    @Autowired
    private UsersService usersService;

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private PaymentsService paymentsService; // Thêm dependency

    @Autowired
    private PaymentsRepository paymentsRepository; // Thêm dependency

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartDTO> getCart(Authentication authentication) {
        Integer userId = Integer.parseInt(authentication.getName());
        logger.info("Fetching cart for user: {}", userId);
        CartDTO cart = cartService.getCart(userId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartDTO> addToCart(@Valid @RequestBody CartAddRequest request, Authentication authentication) {
        logger.info("Adding to cart for user: {}, payload: {}", authentication.getName(), request);
        Integer userId = Integer.parseInt(authentication.getName());
        CartDTO cart = cartService.addToCart(userId, request.getProductId(), request.getQuantity(), request.getSize());
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartDTO> updateCartItemQuantity(
            @Valid @RequestBody CartUpdateRequest request, Authentication authentication) {
        Integer userId = Integer.parseInt(authentication.getName());
        logger.info("Updating cart for user: {}, cartItemId: {}", userId, request.getCartItemId());
        CartDTO cart = cartService.updateCartItem(
                userId, request.getCartItemId(), request.getProductId(), request.getQuantity(), request.getSize());
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/remove")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> removeFromCart(
            @RequestParam Integer productId, Authentication authentication) {
        Integer userId = Integer.parseInt(authentication.getName());
        logger.info("Removing product {} from cart for user: {}", productId, userId);
        cartService.removeFromCart(userId, productId);
        return ResponseEntity.ok("Product removed from cart");
    }

    @DeleteMapping("/clear")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> clearCart(Authentication authentication) {
        Integer userId = Integer.parseInt(authentication.getName());
        logger.info("Clearing cart for user: {}", userId);
        cartService.clearCart(userId);
        return ResponseEntity.ok("Cart cleared");
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> checkout(@Valid @RequestBody CartCheckoutRequest request, Authentication authentication, HttpServletRequest httpRequest) {
        Integer userId = Integer.parseInt(authentication.getName());
        String paymentMethod = request.getPaymentMethod();
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            logger.error("Payment method is missing for userId: {}", userId);
            throw new IllegalArgumentException("Payment method is required");
        }
        logger.info("Checkout for userId: {}, paymentMethod: {}", userId, paymentMethod);
        OrderResponse order = cartService.checkout(userId, paymentMethod);

        if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
            try {
                // Kiểm tra xem đơn hàng đã có giao dịch thanh toán thành công chưa
                List<Payments> existingPayments = paymentsRepository.findByOrderIdOrderByCreatedAtDesc(order.getId());
                if (existingPayments.stream().anyMatch(p -> p.getStatus() == Payments.PaymentStatus.COMPLETED)) {
                    return ResponseEntity.badRequest().body(createErrorResponse("Đơn hàng này đã được thanh toán"));
                }

                // Xóa các bản ghi PENDING trước đó (nếu có) để tránh dư thừa
                List<Payments> pendingPayments = existingPayments.stream()
                        .filter(p -> p.getStatus() == Payments.PaymentStatus.PENDING)
                        .collect(Collectors.toList());
                for (Payments pending : pendingPayments) {
                    paymentsRepository.delete(pending);
                    logger.info("Deleted previous PENDING payment with ID {} for order {}", pending.getId(), order.getId());
                }

                // Tạo vnp_TxnRef duy nhất
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                String vnp_TxnRef = order.getId() + "_" + timestamp;

                // Lưu giao dịch vào Payments
                PaymentsDTO paymentDTO = paymentsService.createPayment(order.getId(), order.getTotalPrice(), paymentMethod, vnp_TxnRef);

                // Tạo payment URL với vnp_TxnRef
                String paymentUrl = vnPayService.createPaymentUrl(order, httpRequest, vnp_TxnRef);

                Map<String, Object> response = new HashMap<>();
                response.put("order", order);
                response.put("paymentUrl", paymentUrl);
                response.put("txnRef", vnp_TxnRef);
                return ResponseEntity.ok(response);
            } catch (UnsupportedEncodingException e) {
                logger.error("Failed to generate VNPay payment URL for userId: {}", userId, e);
                return ResponseEntity.status(500).body(createErrorResponse("Failed to generate VNPay payment URL: " + e.getMessage()));
            }
        }
        return ResponseEntity.ok(order);
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Error");
        response.put("message", message);
        return response;
    }
}