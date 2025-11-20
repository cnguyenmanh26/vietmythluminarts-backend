package com.social.vietmythluminartsbackend.controller;

import com.social.vietmythluminartsbackend.dto.request.CancelOrderRequest;
import com.social.vietmythluminartsbackend.dto.request.CreateOrderRequest;
import com.social.vietmythluminartsbackend.dto.request.UpdateOrderStatusRequest;
import com.social.vietmythluminartsbackend.dto.request.UpdatePaymentStatusRequest;
import com.social.vietmythluminartsbackend.dto.response.ApiResponse;
import com.social.vietmythluminartsbackend.model.Cart;
import com.social.vietmythluminartsbackend.model.Order;
import com.social.vietmythluminartsbackend.model.User;
import com.social.vietmythluminartsbackend.model.enums.OrderStatus;
import com.social.vietmythluminartsbackend.model.enums.PaymentStatus;
import com.social.vietmythluminartsbackend.service.CartService;
import com.social.vietmythluminartsbackend.service.OrderService;
import com.social.vietmythluminartsbackend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for order endpoints
 * Migrated from Node.js orderController.js
 * 
 * Base URL: /api/orders
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final UserService userService;

    /**
     * Create order from cart (Checkout)
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Order>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        Cart cart = cartService.getCart(user);

        Order order = orderService.createOrder(
                user,
                request.getShippingAddress(),
                request.getPaymentMethod(),
                request.getNotes(),
                request.getDiscount(),
                cart
        );

        ApiResponse<Order> response = ApiResponse.success(
                "Order created successfully",
                order
        );

        return ResponseEntity.status(201).body(response);
    }

    /**
     * Get user's orders (Order history)
     * GET /api/orders
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Order>>> getUserOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) OrderStatus status,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, limit, Sort.by("createdAt").descending());

        Page<Order> orders = orderService.getUserOrders(user, pageable, status);

        ApiResponse<Page<Order>> response = ApiResponse.success(
                "Orders retrieved successfully",
                orders
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get order by ID
     * GET /api/orders/:id
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> getOrderById(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userService.findByEmail(userDetails.getUsername());
        Order order = orderService.getOrderById(id, currentUser);

        ApiResponse<Order> response = ApiResponse.success(
                "Order retrieved successfully",
                order
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Update order status (Admin only)
     * PUT /api/orders/:id/status
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Order>> updateOrderStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User admin = userService.findByEmail(userDetails.getUsername());
        Order order = orderService.updateOrderStatus(id, request.getStatus(), request.getNote(), admin);

        ApiResponse<Order> response = ApiResponse.success(
                "Order status updated to " + request.getStatus(),
                order
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Update payment status (Admin only)
     * PUT /api/orders/:id/payment
     */
    @PutMapping("/{id}/payment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Order>> updatePaymentStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdatePaymentStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User admin = userService.findByEmail(userDetails.getUsername());
        Order order = orderService.updatePaymentStatus(
                id, request.getPaymentStatus(), request.getTransactionId(), admin
        );

        ApiResponse<Order> response = ApiResponse.success(
                "Payment status updated to " + request.getPaymentStatus(),
                order
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Cancel order (User can cancel if status is Pending/Confirmed)
     * PUT /api/orders/:id/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Order>> cancelOrder(
            @PathVariable String id,
            @Valid @RequestBody CancelOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        Order order = orderService.cancelOrder(id, request.getReason(), user);

        ApiResponse<Order> response = ApiResponse.success(
                "Order cancelled successfully",
                order
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get all orders (Admin only)
     * GET /api/orders/admin/all
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<Order>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) PaymentStatus paymentStatus
    ) {
        Pageable pageable = PageRequest.of(page, limit, Sort.by("createdAt").descending());

        Page<Order> orders = orderService.getAllOrders(pageable, status, paymentStatus);

        ApiResponse<Page<Order>> response = ApiResponse.success(
                "All orders retrieved successfully",
                orders
        );

        return ResponseEntity.ok(response);
    }
}

