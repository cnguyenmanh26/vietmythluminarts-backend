package com.social.vietmythluminartsbackend.service;

import com.social.vietmythluminartsbackend.exception.ResourceNotFoundException;
import com.social.vietmythluminartsbackend.model.Cart;
import com.social.vietmythluminartsbackend.model.Product;
import com.social.vietmythluminartsbackend.model.User;
import com.social.vietmythluminartsbackend.model.embedded.CartItem;
import com.social.vietmythluminartsbackend.repository.CartRepository;
import com.social.vietmythluminartsbackend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for cart operations
 * Migrated from Node.js cartController.js
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    /**
     * Get user's cart (create if not exists)
     */
    public Cart getCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    /**
     * Add item to cart
     */
    @Transactional
    public Cart addToCart(User user, String productId, Integer quantity) {
        // Validate
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        // Get product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Check stock
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException(
                    String.format("Insufficient stock. Only %d items available", product.getStock())
            );
        }

        // Get or create cart
        Cart cart = getCart(user);

        // Check if product already in cart
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> productId.equals(item.getProduct().getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Update quantity
            int newQuantity = existingItem.getQuantity() + quantity;
            
            if (product.getStock() < newQuantity) {
                throw new IllegalArgumentException(
                        String.format("Cannot add more. Only %d items available", product.getStock())
                );
            }

            existingItem.setQuantity(newQuantity);
            // Update price to use discounted price if available
            existingItem.setPrice(product.getEffectivePrice());
        } else {
            // Add new item
            Double finalPrice = product.getEffectivePrice();
            
            CartItem.ProductSnapshot snapshot = CartItem.ProductSnapshot.builder()
                    .name(product.getName())
                    .image(product.getPrimaryImage())
                    .category(product.getCategory())
                    .build();

            CartItem newItem = CartItem.builder()
                    .product(product)
                    .quantity(quantity)
                    .price(finalPrice)
                    .productSnapshot(snapshot)
                    .build();

            cart.getItems().add(newItem);
        }

        // Calculate totals
        cart.calculateTotals();

        return cartRepository.save(cart);
    }

    /**
     * Update cart item quantity
     */
    @Transactional
    public Cart updateCartItem(User user, String productId, Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        // Check product stock
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getStock() < quantity) {
            throw new IllegalArgumentException(
                    String.format("Insufficient stock. Only %d items available", product.getStock())
            );
        }

        Cart cart = getCart(user);

        CartItem item = cart.getItems().stream()
                .filter(i -> productId.equals(i.getProduct().getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

        item.setQuantity(quantity);
        // Update price - use discounted price if available
        item.setPrice(product.getEffectivePrice());

        cart.calculateTotals();

        return cartRepository.save(cart);
    }

    /**
     * Remove item from cart
     */
    @Transactional
    public Cart removeFromCart(User user, String productId) {
        Cart cart = getCart(user);

        boolean removed = cart.getItems().removeIf(item -> productId.equals(item.getProduct().getId()));

        if (!removed) {
            throw new ResourceNotFoundException("Item not found in cart");
        }

        cart.calculateTotals();

        return cartRepository.save(cart);
    }

    /**
     * Clear cart
     */
    @Transactional
    public Cart clearCart(User user) {
        Cart cart = getCart(user);
        cart.clear();
        return cartRepository.save(cart);
    }
}

