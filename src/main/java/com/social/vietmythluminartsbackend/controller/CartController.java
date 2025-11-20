package com.social.vietmythluminartsbackend.controller;

import com.social.vietmythluminartsbackend.dto.request.AddToCartRequest;
import com.social.vietmythluminartsbackend.dto.request.UpdateCartItemRequest;
import com.social.vietmythluminartsbackend.dto.response.ApiResponse;
import com.social.vietmythluminartsbackend.model.Cart;
import com.social.vietmythluminartsbackend.model.User;
import com.social.vietmythluminartsbackend.service.CartService;
import com.social.vietmythluminartsbackend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for cart endpoints
 * Migrated from Node.js cartController.js
 * 
 * Base URL: /api/cart
 */
@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    /**
     * Get user's cart
     * GET /api/cart
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Cart>> getCart(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        Cart cart = cartService.getCart(user);

        ApiResponse<Cart> response = ApiResponse.success(
                "Cart retrieved successfully",
                cart
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Add item to cart
     * POST /api/cart/items
     */
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<Cart>> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        Cart cart = cartService.addToCart(user, request.getProductId(), request.getQuantity());

        ApiResponse<Cart> response = ApiResponse.success(
                "Item added to cart successfully",
                cart
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Update cart item quantity
     * PUT /api/cart/items/:productId
     */
    @PutMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<Cart>> updateCartItem(
            @PathVariable String productId,
            @Valid @RequestBody UpdateCartItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        Cart cart = cartService.updateCartItem(user, productId, request.getQuantity());

        ApiResponse<Cart> response = ApiResponse.success(
                "Cart item updated successfully",
                cart
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Remove item from cart
     * DELETE /api/cart/items/:productId
     */
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<Cart>> removeFromCart(
            @PathVariable String productId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        Cart cart = cartService.removeFromCart(user, productId);

        ApiResponse<Cart> response = ApiResponse.success(
                "Item removed from cart successfully",
                cart
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Clear cart
     * DELETE /api/cart
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Cart>> clearCart(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        Cart cart = cartService.clearCart(user);

        ApiResponse<Cart> response = ApiResponse.success(
                "Cart cleared successfully",
                cart
        );

        return ResponseEntity.ok(response);
    }
}

