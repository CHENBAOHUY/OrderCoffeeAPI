package com.example.springbootapi.Controller;

import com.example.springbootapi.Service.CartService;
import com.example.springbootapi.Service.UsersService;
import com.example.springbootapi.dto.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    @Autowired
    private UsersService usersService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<CartDTO> getCart(
            @RequestParam(required = false) Integer userId,
            Authentication authentication) {
        Integer targetUserId = getTargetUserId(userId, authentication);
        CartDTO cart = cartService.getCart(targetUserId, authentication);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<CartDTO> addToCart(@Valid @RequestBody CartAddRequest request, Authentication authentication) {
        Integer targetUserId = getTargetUserId(request.getUserId(), authentication);
        CartDTO cart = cartService.addToCart(targetUserId, request.getProductId(), request.getQuantity(), authentication);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/update")
    public ResponseEntity<CartDTO> updateCartItemQuantity(
            @RequestBody CartUpdateRequest request,
            Authentication authentication) {
        Integer userId = usersService.getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(cartService.updateCartItem(userId, request.getCartItemId(), request.getProductId(), request.getQuantity(), authentication));
    }

    @DeleteMapping("/remove")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<?> removeFromCart(
            @RequestParam(required = false) Integer userId,
            @RequestParam Integer productId,
            Authentication authentication) {
        Integer targetUserId = getTargetUserId(userId, authentication);
        cartService.removeFromCart(targetUserId, productId, authentication);
        return ResponseEntity.ok("Product removed from cart");
    }

    @DeleteMapping("/clear")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<?> clearCart(
            @RequestParam(required = false) Integer userId,
            Authentication authentication) {
        Integer targetUserId = getTargetUserId(userId, authentication);
        cartService.clearCart(targetUserId, authentication);
        return ResponseEntity.ok("Cart cleared");
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<OrderResponse> checkout(@Valid @RequestBody CartCheckoutRequest request, Authentication authentication) {
        Integer authenticatedUserId = Integer.parseInt(authentication.getName());

        if (request == null) {
            logger.error("Request body is missing for userId: {}", authenticatedUserId);
            throw new IllegalArgumentException("Request body is required");
        }

        String paymentMethod = request.getPaymentMethod();
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            logger.error("Payment method is missing or empty in request for userId: {}", authenticatedUserId);
            throw new IllegalArgumentException("Payment method is required and cannot be empty");
        }

        logger.info("Received checkout request for userId: {}, paymentMethod: {}", authenticatedUserId, paymentMethod);
        OrderResponse order = cartService.checkout(authenticatedUserId, paymentMethod, authentication);
        return ResponseEntity.ok(order);
    }

    private Integer getTargetUserId(Integer userId, Authentication authentication) {
        Integer authenticatedUserId = Integer.parseInt(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        if (userId != null) {
            if (isAdmin) {
                logger.info("Admin {} accessing cart of user {}", authenticatedUserId, userId);
                return userId;
            } else if (!userId.equals(authenticatedUserId)) {
                logger.warn("User {} attempted to access cart of user {}", authenticatedUserId, userId);
                throw new SecurityException("You do not have permission to access this cart");
            }
        }
        return authenticatedUserId;
    }
}