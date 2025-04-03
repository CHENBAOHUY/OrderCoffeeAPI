package com.example.springbootapi.Controller;

import com.example.springbootapi.Service.CartService;
import com.example.springbootapi.dto.CartDTO;
import com.example.springbootapi.dto.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

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
    public ResponseEntity<CartDTO> addToCart(
            @RequestParam(required = false) Integer userId,
            @RequestParam Integer productId,
            @RequestParam Integer quantity,
            Authentication authentication) {
        Integer targetUserId = getTargetUserId(userId, authentication);
        CartDTO cart = cartService.addToCart(targetUserId, productId, quantity, authentication);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<CartDTO> updateCartItemQuantity(
            @RequestParam(required = false) Integer userId,
            @RequestParam Integer productId,
            @RequestParam Integer quantity,
            Authentication authentication) {
        Integer targetUserId = getTargetUserId(userId, authentication);
        CartDTO cart = cartService.updateCartItemQuantity(targetUserId, productId, quantity, authentication);
        return ResponseEntity.ok(cart);
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
    public ResponseEntity<OrderResponse> checkout(
            @RequestParam(required = false) Integer userId,
            @RequestParam String paymentMethod,
            Authentication authentication) {
        Integer targetUserId = getTargetUserId(userId, authentication);
        OrderResponse order = cartService.checkout(targetUserId, paymentMethod, authentication);
        return ResponseEntity.ok(order);
    }

    private Integer getTargetUserId(Integer userId, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        Integer authenticatedUserId = Integer.parseInt(authentication.getName());
        if (userId == null) {
            return authenticatedUserId;
        }
        if (isAdmin) {
            return userId;
        } else {
            if (!userId.equals(authenticatedUserId)) {
                throw new SecurityException("You do not have permission to access this cart");
            }
            return userId;
        }
    }
}