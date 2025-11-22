package com.social.back_java.service.impl;

import com.social.back_java.model.Cart;
import com.social.back_java.model.CartItem;
import com.social.back_java.model.Product;
import com.social.back_java.model.ProductSnapshot;
import com.social.back_java.model.User;
import com.social.back_java.repository.CartRepository;
import com.social.back_java.repository.ProductRepository;
import com.social.back_java.repository.UserRepository;
import com.social.back_java.service.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Optional<Cart> getCartByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Optional<Cart> cart = cartRepository.findByUser(user);
        
        // Create empty cart if not exists
        if (cart.isEmpty()) {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return Optional.of(cartRepository.save(newCart));
        }
        
        return cart;
    }

    @Override
    public Cart addItemToCart(Long userId, CartItem item) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Validate product exists and has stock
        Product product = productRepository.findById(item.getProduct().getId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (product.getStock() < item.getQuantity()) {
            throw new RuntimeException("Insufficient stock. Only " + product.getStock() + " items available");
        }
        
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return newCart;
        });

        // Check if product already in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity
            CartItem existing = existingItem.get();
            int newQuantity = existing.getQuantity() + item.getQuantity();
            
            if (product.getStock() < newQuantity) {
                throw new RuntimeException("Cannot add more. Only " + product.getStock() + " items available");
            }
            
            existing.setQuantity(newQuantity);
            // Update price to use discounted price if available
            existing.setPrice(product.getPricegiamgia() != null ? product.getPricegiamgia() : product.getPrice());
        } else {
            // Add new item - use discounted price if available
            Double finalPrice = product.getPricegiamgia() != null ? product.getPricegiamgia() : product.getPrice();
            item.setPrice(finalPrice);
            item.setProduct(product);
            
            // Create product snapshot
            ProductSnapshot snapshot = new ProductSnapshot();
            snapshot.setName(product.getName());
            snapshot.setImage(product.getImages().isEmpty() ? null : product.getImages().get(0).getUrl());
            snapshot.setCategory(product.getCategory());
            item.setProductSnapshot(snapshot);
            
            cart.getItems().add(item);
        }

        return cartRepository.save(cart);
    }

    @Override
    public Cart removeItemFromCart(Long userId, Long itemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        boolean removed = cart.getItems().removeIf(item -> item.getId().equals(itemId));
        
        if (!removed) {
            throw new RuntimeException("Item not found in cart");
        }
        
        return cartRepository.save(cart);
    }

    @Override
    public Cart updateItemQuantity(Long userId, Long itemId, int quantity) {
        if (quantity < 1) {
            throw new RuntimeException("Quantity must be at least 1");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        // Check product stock
        Product product = productRepository.findById(cartItem.getProduct().getId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock. Only " + product.getStock() + " items available");
        }
        
        cartItem.setQuantity(quantity);
        // Update price - use discounted price if available
        cartItem.setPrice(product.getPricegiamgia() != null ? product.getPricegiamgia() : product.getPrice());

        return cartRepository.save(cart);
    }

    @Override
    public void clearCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        cartRepository.findByUser(user).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }
}
