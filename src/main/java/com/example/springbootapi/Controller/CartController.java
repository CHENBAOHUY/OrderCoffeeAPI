package com.example.springbootapi.Controller;

import com.example.springbootapi.Entity.Cart;
import com.example.springbootapi.Service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public List<Cart> getAllCartItems() {
        return cartService.getAllCartItems();
    }

    @GetMapping("/{id}")
    public Optional<Cart> getCartItemById(@PathVariable Integer id) {
        return cartService.getCartItemById(id);
    }

    @PostMapping
    public Cart addCartItem(@RequestBody Cart cart) {
        return cartService.addCartItem(cart);
    }

    @DeleteMapping("/{id}")
    public void removeCartItem(@PathVariable Integer id) {
        cartService.removeCartItem(id);
    }
}
