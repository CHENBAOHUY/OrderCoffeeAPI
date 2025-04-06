package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.*;
import com.example.springbootapi.dto.*;
import com.example.springbootapi.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemsRepository cartItemsRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private UserRepository usersRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private PaymentsRepository paymentsRepository; // Thêm repository cho Payments

    // Ánh xạ từ entity sang DTO (không thay đổi các phương thức ánh xạ hiện có)

    private UserDto toUserDto(Users user) {
        return new UserDto(user.getId(), user.getName());
    }

    private ProductResponseDTO toProductResponseDTO(Products product) {
        ProductResponseDTO productDTO = new ProductResponseDTO();
        productDTO.setId(product.getId());
        productDTO.setName(product.getName());
        productDTO.setDescription(product.getDescription());
        productDTO.setPrice(BigDecimal.valueOf(product.getPrice()));
        productDTO.setImage(product.getImage());
        productDTO.setCategoryId(product.getCategories() != null ? product.getCategories().getId() : null);
        return productDTO;
    }

    private CartItemDTO toCartItemDTO(CartItems cartItem) {
        CartItemDTO cartItemDTO = new CartItemDTO();
        cartItemDTO.setId(cartItem.getId());
        cartItemDTO.setProduct(toProductResponseDTO(cartItem.getProduct()));
        cartItemDTO.setQuantity(cartItem.getQuantity());
        cartItemDTO.setAddedAt(cartItem.getAddedAt());
        return cartItemDTO;
    }

    private CartDTO toCartDTO(Cart cart) {
        CartDTO cartDTO = new CartDTO();
        cartDTO.setId(cart.getId());
        cartDTO.setUser(toUserDto(cart.getUser()));
        cartDTO.setCreatedAt(cart.getCreatedAt());
        if (cart.getCartItems() != null) {
            cartDTO.setCartItems(cart.getCartItems().stream()
                    .map(this::toCartItemDTO)
                    .collect(Collectors.toList()));
        } else {
            cartDTO.setCartItems(new ArrayList<>());
        }
        return cartDTO;
    }

    private OrderDetailResponse toOrderDetailResponse(OrderDetails orderDetail) {
        return new OrderDetailResponse(orderDetail);
    }

    private OrderResponse toOrderResponse(Orders order) {
        OrderResponse orderResponse = new OrderResponse(order);
        if (order.getOrderDetails() != null) {
            orderResponse.setOrderDetails(order.getOrderDetails().stream()
                    .map(this::toOrderDetailResponse)
                    .collect(Collectors.toList()));
        }
        return orderResponse;
    }

    private void checkAccess(Authentication authentication, Integer userId) {
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        Integer authenticatedUserId = Integer.parseInt(authentication.getName());
        if (!isAdmin && !authenticatedUserId.equals(userId)) {
            throw new SecurityException("You do not have permission to access this cart");
        }
    }

    @Transactional
    public Cart getOrCreateCart(Integer userId, Authentication authentication) {
        checkAccess(authentication, userId);
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        if (cartOpt.isPresent()) {
            return cartOpt.get();
        }
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    @Transactional
    public CartDTO addToCart(Integer userId, Integer productId, Integer quantity, Authentication authentication) {
        checkAccess(authentication, userId);
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        Cart cart = getOrCreateCart(userId, authentication);
        Products product = productsRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Optional<CartItems> cartItemOpt = cartItemsRepository.findByCart_IdAndProduct_Id(cart.getId(), productId);
        if (cartItemOpt.isPresent()) {
            CartItems cartItem = cartItemOpt.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItemsRepository.save(cartItem);
        } else {
            CartItems cartItem = new CartItems();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItemsRepository.save(cartItem);
        }
        return toCartDTO(cartRepository.findById(cart.getId()).get());
    }

    @Transactional
    public CartDTO updateCartItemQuantity(Integer userId, Integer productId, Integer quantity, Authentication authentication) {
        checkAccess(authentication, userId);
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        Cart cart = getOrCreateCart(userId, authentication);
        Optional<CartItems> cartItemOpt = cartItemsRepository.findByCart_IdAndProduct_Id(cart.getId(), productId);
        if (cartItemOpt.isEmpty()) {
            throw new RuntimeException("Product not found in cart");
        }
        CartItems cartItem = cartItemOpt.get();
        cartItem.setQuantity(quantity);
        cartItemsRepository.save(cartItem);
        return toCartDTO(cartRepository.findById(cart.getId()).get());
    }

    @Transactional
    public void removeFromCart(Integer userId, Integer productId, Authentication authentication) {
        checkAccess(authentication, userId);
        Cart cart = getOrCreateCart(userId, authentication);
        Optional<CartItems> cartItemOpt = cartItemsRepository.findByCart_IdAndProduct_Id(cart.getId(), productId);
        if (cartItemOpt.isPresent()) {
            cartItemsRepository.delete(cartItemOpt.get());
        } else {
            throw new RuntimeException("Product not found in cart");
        }
    }

    public CartDTO getCart(Integer userId, Authentication authentication) {
        checkAccess(authentication, userId);
        return toCartDTO(getOrCreateCart(userId, authentication));
    }

    @Transactional
    public void clearCart(Integer userId, Authentication authentication) {
        checkAccess(authentication, userId);
        Cart cart = getOrCreateCart(userId, authentication);
        cartItemsRepository.deleteAll(cart.getCartItems());
    }

    @Transactional
    public OrderResponse checkout(Integer userId, String paymentMethod, Authentication authentication) {
        checkAccess(authentication, userId);
        Cart cart = getOrCreateCart(userId, authentication);
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        Orders order = new Orders();
        order.setUser(cart.getUser());
        order.setStatus(Orders.OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        double totalPrice = 0.0;
        for (CartItems cartItem : cart.getCartItems()) {
            OrderDetails orderDetail = new OrderDetails();
            orderDetail.setOrder(order);
            orderDetail.setProduct(cartItem.getProduct());
            orderDetail.setQuantity(cartItem.getQuantity());
            orderDetail.setItemTotalPrice(cartItem.getProduct().getPrice() * cartItem.getQuantity());
            orderDetail.setUnitPrice(cartItem.getProduct().getPrice()); // Thêm unitPrice
            order.getOrderDetails().add(orderDetail);
            totalPrice += orderDetail.getItemTotalPrice();
        }
        order.setTotalPrice(totalPrice);

        // Tạo bản ghi thanh toán
        Payments payment = new Payments();
        payment.setOrder(order);
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(totalPrice);
        payment.setStatus(Payments.PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDateTime.now());
        order.getPayments().add(payment);

        // Lưu đơn hàng
        ordersRepository.save(order);

        // Xóa giỏ hàng
        clearCart(userId, authentication);

        return toOrderResponse(order);
    }
}