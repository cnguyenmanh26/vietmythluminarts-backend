package com.social.back_java.controller;

import com.social.back_java.model.Cart;
import com.social.back_java.model.CartItem;
import com.social.back_java.service.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private ICartService cartService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<Cart> getCartByUserId(@PathVariable Long userId) {
        return cartService.getCartByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/user/{userId}/add")
    public ResponseEntity<Cart> addItemToCart(@PathVariable Long userId, @RequestBody CartItem item) {
        try {
            return ResponseEntity.ok(cartService.addItemToCart(userId, item));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/user/{userId}/remove/{itemId}")
    public ResponseEntity<Cart> removeItemFromCart(@PathVariable Long userId, @PathVariable Long itemId) {
        try {
            return ResponseEntity.ok(cartService.removeItemFromCart(userId, itemId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/user/{userId}/update/{itemId}")
    public ResponseEntity<Cart> updateItemQuantity(
            @PathVariable Long userId,
            @PathVariable Long itemId,
            @RequestParam int quantity) {
        try {
            return ResponseEntity.ok(cartService.updateItemQuantity(userId, itemId, quantity));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/user/{userId}/clear")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok().build();
    }
}
