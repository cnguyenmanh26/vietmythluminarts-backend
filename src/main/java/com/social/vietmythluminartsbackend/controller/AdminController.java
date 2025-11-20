package com.social.vietmythluminartsbackend.controller;

import com.social.vietmythluminartsbackend.dto.request.UpdateUserStatusRequest;
import com.social.vietmythluminartsbackend.dto.response.ApiResponse;
import com.social.vietmythluminartsbackend.dto.response.UserResponse;
import com.social.vietmythluminartsbackend.model.Order;
import com.social.vietmythluminartsbackend.model.User;
import com.social.vietmythluminartsbackend.model.enums.OrderStatus;
import com.social.vietmythluminartsbackend.model.enums.PaymentStatus;
import com.social.vietmythluminartsbackend.model.enums.Role;
import com.social.vietmythluminartsbackend.repository.UserRepository;
import com.social.vietmythluminartsbackend.service.OrderService;
import com.social.vietmythluminartsbackend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for admin endpoints
 * Migrated from Node.js admin endpoints
 * 
 * Base URL: /api/admin
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final OrderService orderService;
    private final UserRepository userRepository;

    /**
     * Get all users (Admin only)
     * GET /api/admin/users
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<User>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String search
    ) {
        Pageable pageable = PageRequest.of(page, limit, Sort.by("createdAt").descending());

        Page<User> users;
        
        if (search != null && !search.isEmpty()) {
            // Search by name or email
            List<User> userList = userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search);
            // Convert to Page using PageImpl
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), userList.size());
            List<User> pageContent = start < userList.size() ? userList.subList(start, end) : new java.util.ArrayList<>();
            users = new PageImpl<>(pageContent, pageable, userList.size());
        } else if (role != null && isActive != null) {
            List<User> userList = userRepository.findByRoleAndIsActive(role, isActive);
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), userList.size());
            List<User> pageContent = start < userList.size() ? userList.subList(start, end) : new java.util.ArrayList<>();
            users = new PageImpl<>(pageContent, pageable, userList.size());
        } else {
            // Get all users with pagination
            users = userRepository.findAll(pageable);
        }

        ApiResponse<Page<User>> response = ApiResponse.success(
                "Users retrieved successfully",
                users
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get user by ID (Admin only)
     * GET /api/admin/users/:id
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String id) {
        UserResponse user = userService.getUserById(id);

        ApiResponse<UserResponse> response = ApiResponse.success(
                "User retrieved successfully",
                user
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Update user status (Admin only)
     * PUT /api/admin/users/:id/status
     */
    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Prevent admin from deactivating themselves
        User currentAdmin = userService.findByEmail(userDetails.getUsername());
        if (currentAdmin.getId().equals(id) && Boolean.FALSE.equals(request.getIsActive())) {
            throw new IllegalArgumentException("You cannot deactivate your own account");
        }

        User user = userService.findById(id);
        
        if (Boolean.TRUE.equals(request.getIsActive())) {
            userService.reactivateAccount(id);
        } else {
            userService.deactivateAccount(id);
        }

        UserResponse userResponse = userService.getUserById(id);

        ApiResponse<UserResponse> response = ApiResponse.success(
                "User status updated successfully",
                userResponse
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get all orders (Admin only)
     * GET /api/admin/orders
     */
    @GetMapping("/orders")
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

    /**
     * Get dashboard statistics (Admin only)
     * GET /api/admin/dashboard/stats
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<Object>> getDashboardStats() {
        // TODO: Implement dashboard statistics
        // - Total users
        // - Total orders
        // - Total revenue
        // - Recent orders
        // - Popular products
        // etc.

        ApiResponse<Object> response = ApiResponse.success(
                "Dashboard statistics retrieved successfully",
                null // TODO: Implement statistics
        );

        return ResponseEntity.ok(response);
    }
}

