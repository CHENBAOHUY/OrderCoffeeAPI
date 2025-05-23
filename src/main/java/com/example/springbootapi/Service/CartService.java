package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.*;
import com.example.springbootapi.dto.*;
import com.example.springbootapi.repository.CartRepository;
import com.example.springbootapi.repository.OrdersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {
    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final UsersService usersService;
    private final ProductsService productsService;
    private final OrdersRepository ordersRepository;
    private final LoyaltyPointsService loyaltyPointsService;

    @Autowired
    public CartService(CartRepository cartRepository, UsersService usersService,
                       ProductsService productsService, OrdersRepository ordersRepository,
                       LoyaltyPointsService loyaltyPointsService) {
        this.cartRepository = cartRepository;
        this.usersService = usersService;
        this.productsService = productsService;
        this.ordersRepository = ordersRepository;
        this.loyaltyPointsService = loyaltyPointsService;
    }

    public CartDTO getCart(Integer userId) {
        logger.info("Fetching cart for userId: {}", userId);
        Optional<Cart> cartOpt = cartRepository.findByUser_Id(userId);
        Cart cart = cartOpt.orElseGet(() -> {
            Cart newCart = new Cart();
            Users user = usersService.getUserById(userId);
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });

        CartDTO cartDTO = new CartDTO();
        cartDTO.setId(cart.getId()); // Thêm dòng này
        Users user = usersService.getUserById(userId);
        UserDto userDto = new UserDto(user.getId(), user.getName());
        cartDTO.setUser(userDto);

        cartDTO.setCartItems(cart.getCartItems().stream().map(cartItem -> {
            CartItemDTO item = new CartItemDTO();
            item.setId(cartItem.getId());
            Products productEntity = cartItem.getProduct();
            ProductResponseDTO product = new ProductResponseDTO();
            product.setId(productEntity.getId());
            product.setName(productEntity.getName());
            product.setPrice(productEntity.getPrice());
            product.setCategoryId(productEntity.getCategories().getId());
            item.setProduct(product);
            item.setQuantity(cartItem.getQuantity());
            item.setSize(cartItem.getSize());
            item.setAddedAt(cartItem.getAddedAt());
            return item;
        }).collect(Collectors.toList()));

        cartDTO.setCreatedAt(cart.getCreatedAt());
        return cartDTO;
    }

    @Transactional
    public CartDTO addToCart(Integer userId, Integer productId, Integer quantity, String size) {
        logger.info("Adding to cart for userId: {}, productId: {}, quantity: {}, size: {}", userId, productId, quantity, size);
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (size == null || !size.matches("^(S|M|L)$")) {
            throw new IllegalArgumentException("Size must be S, M, or L");
        }

        Users user = usersService.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }

        Optional<Cart> cartOpt = cartRepository.findByUser_Id(userId);
        Cart cart = cartOpt.orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });

        Products product = productsService.getProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));

        cart.getCartItems().removeIf(item -> item.getProduct() == null);

        Optional<CartItems> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct() != null && item.getProduct().getId().equals(productId) && item.getSize().equals(size))
                .findFirst();

        CartItems cartItem;
        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            cartItem = new CartItems();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setSize(size);
            cart.getCartItems().add(cartItem);
        }

        cartRepository.save(cart);
        return getCart(userId);
    }

    public CartDTO updateCartItem(Integer userId, Integer cartItemId, Integer productId, Integer quantity, String size) {
        logger.info("Updating cart for userId: {}, cartItemId: {}", userId, cartItemId);
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (size == null || !size.matches("^(S|M|L)$")) {
            throw new IllegalArgumentException("Size must be S, M, or L");
        }

        Cart cart = cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + userId));
        CartItems cartItem = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId) && item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        cartItem.setQuantity(quantity);
        cartItem.setSize(size);
        cartRepository.save(cart);
        return getCart(userId);
    }

    public void removeFromCart(Integer userId, Integer productId) {
        logger.info("Removing product {} from cart for userId: {}", productId, userId);
        Optional<Cart> cartOpt = cartRepository.findByUser_Id(userId);
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            cart.getCartItems().removeIf(item -> item.getProduct().getId().equals(productId));
            cartRepository.save(cart);
        }
    }

    public void clearCart(Integer userId) {
        logger.info("Clearing cart for userId: {}", userId);
        Optional<Cart> cartOpt = cartRepository.findByUser_Id(userId);
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            cart.getCartItems().clear();
            cartRepository.save(cart);
        }
    }

    @Transactional
    public OrderResponse checkout(Integer userId, String paymentMethod) {
        logger.info("Starting checkout for userId: {}, paymentMethod: {}", userId, paymentMethod);
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            logger.error("Payment method is null or empty for userId: {}", userId);
            throw new IllegalArgumentException("Payment method cannot be null or empty");
        }

        Optional<Cart> cartOpt = cartRepository.findByUser_Id(userId);
        if (cartOpt.isEmpty() || cartOpt.get().getCartItems().isEmpty()) {
            logger.warn("Cart is empty for userId: {}", userId);
            throw new RuntimeException("Giỏ hàng trống!");
        }
        Cart cart = cartOpt.get();
        Users user = usersService.getUserById(userId);

        Orders order = new Orders();
        order.setUser(user);
        BigDecimal totalPrice = cart.getCartItems().stream()
                .map(cartItem -> cartItem.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalPrice(totalPrice);
        order.setStatus(Orders.OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());

        List<OrderDetails> orderDetailsList = cart.getCartItems().stream().map(cartItem -> {
            OrderDetails detail = new OrderDetails();
            detail.setOrder(order);
            detail.setProduct(cartItem.getProduct());
            detail.setQuantity(cartItem.getQuantity());
            detail.setUnitPrice(cartItem.getProduct().getPrice());
            detail.setItemTotalPrice(cartItem.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            detail.setSize(cartItem.getSize());
            return detail;
        }).collect(Collectors.toList());
        order.setOrderDetails(orderDetailsList);

        Payments payment = new Payments();
        payment.setOrder(order);
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus(Payments.PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setAmount(totalPrice);
        order.getPayments().add(payment);

        logger.info("Saving order with paymentMethod: {}", payment.getPaymentMethod());
        ordersRepository.save(order);

        loyaltyPointsService.addPoints(userId, totalPrice, "VND");

        cart.getCartItems().clear();
        cartRepository.save(cart);

        logger.info("Checkout completed for userId: {}", userId);
        return new OrderResponse(order);
    }

    public List<Cart> getAllCartItems() {
        return cartRepository.findAll();
    }

    public Optional<Cart> getCartItemById(Integer id) {
        return cartRepository.findById(id);
    }

    public Cart addCartItem(Cart cart) {
        return cartRepository.save(cart);
    }

    public void removeCartItem(Integer id) {
        cartRepository.deleteById(id);
    }
}