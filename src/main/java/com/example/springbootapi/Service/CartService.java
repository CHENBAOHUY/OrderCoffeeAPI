package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.Cart;
import com.example.springbootapi.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    private final CartRepository cartRepository;

    @Autowired
    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
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
