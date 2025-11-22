package com.social.back_java.service;

import com.social.back_java.model.Cart;
import com.social.back_java.model.CartItem;

import java.util.Optional;

public interface ICartService {
    Optional<Cart> getCartByUserId(Long userId);
    Cart addItemToCart(Long userId, CartItem item);
    Cart removeItemFromCart(Long userId, Long itemId);
    Cart updateItemQuantity(Long userId, Long itemId, int quantity);
    void clearCart(Long userId);
}
