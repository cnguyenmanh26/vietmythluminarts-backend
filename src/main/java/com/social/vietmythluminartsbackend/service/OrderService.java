package com.social.vietmythluminartsbackend.service;

import com.social.vietmythluminartsbackend.exception.AuthException;
import com.social.vietmythluminartsbackend.exception.ResourceNotFoundException;
import com.social.vietmythluminartsbackend.model.*;
import com.social.vietmythluminartsbackend.model.embedded.Address;
import com.social.vietmythluminartsbackend.model.embedded.OrderItem;
import com.social.vietmythluminartsbackend.model.embedded.StatusHistory;
import com.social.vietmythluminartsbackend.model.enums.OrderStatus;
import com.social.vietmythluminartsbackend.model.enums.PaymentMethod;
import com.social.vietmythluminartsbackend.model.enums.PaymentStatus;
import com.social.vietmythluminartsbackend.model.enums.Role;
import com.social.vietmythluminartsbackend.repository.OrderRepository;
import com.social.vietmythluminartsbackend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for order operations
 * Migrated from Node.js orderController.js
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    /**
     * Create order from cart (Checkout)
     */
    @Transactional
    public Order createOrder(User user, Address shippingAddress, PaymentMethod paymentMethod,
                             String notes, Double discount, Cart cart) {
        // Validate cart
        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // Validate shipping address
        if (shippingAddress == null || shippingAddress.getFullName() == null ||
            shippingAddress.getPhone() == null || shippingAddress.getStreet() == null ||
            shippingAddress.getCity() == null) {
            throw new IllegalArgumentException(
                    "Complete shipping address is required (fullName, phone, street, city)"
            );
        }

        // Verify stock and prepare order items
        List<OrderItem> orderItems = new ArrayList<>();
        double subtotal = 0.0;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            if (product == null) {
                throw new ResourceNotFoundException(
                        "Product " + cartItem.getProductSnapshot().getName() + " no longer exists"
                );
            }

            if (product.getStock() < cartItem.getQuantity()) {
                throw new IllegalArgumentException(
                        String.format("Insufficient stock for %s. Only %d available",
                                product.getName(), product.getStock())
                );
            }

            // Use discounted price if available
            Double finalPrice = product.getEffectivePrice();
            Double itemSubtotal = finalPrice * cartItem.getQuantity();
            subtotal += itemSubtotal;

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .name(product.getName())
                    .image(product.getPrimaryImage())
                    .category(product.getCategory())
                    .price(finalPrice)
                    .quantity(cartItem.getQuantity())
                    .subtotal(itemSubtotal)
                    .build();

            orderItems.add(orderItem);
        }

        // Calculate tax (10%)
        double tax = subtotal * 0.1;

        // Calculate total
        double total = subtotal + tax - (discount != null ? discount : 0.0);

        // Generate order number
        long count = orderRepository.count();
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String orderNumber = String.format("ORD%s%05d", dateStr, count + 1);

        // Create order
        Order order = Order.builder()
                .user(user)
                .orderNumber(orderNumber)
                .items(orderItems)
                .subtotal(subtotal)
                .tax(tax)
                .discount(discount != null ? discount : 0.0)
                .total(total)
                .shippingAddress(shippingAddress)
                .paymentMethod(paymentMethod != null ? paymentMethod : PaymentMethod.COD)
                .paymentStatus(PaymentStatus.Pending)
                .status(OrderStatus.Pending)
                .notes(notes)
                .build();

        // Add initial status history
        StatusHistory initialHistory = StatusHistory.builder()
                .status(OrderStatus.Pending.name())
                .note("Order created")
                .updatedBy(user)
                .timestamp(LocalDateTime.now())
                .build();
        order.getStatusHistory().add(initialHistory);

        // Update product stock
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            product.reduceStock(cartItem.getQuantity());
            productRepository.save(product);
        }

        // Clear cart
        cartService.clearCart(user);

        Order savedOrder = orderRepository.save(order);
        log.info("Order created: {} by user: {}", savedOrder.getOrderNumber(), user.getEmail());

        return savedOrder;
    }

    /**
     * Get user's orders
     */
    public Page<Order> getUserOrders(User user, Pageable pageable, OrderStatus status) {
        if (status != null) {
            return orderRepository.findByUserAndStatus(user, status, pageable);
        }
        return orderRepository.findByUser(user, pageable);
    }

    /**
     * Get order by ID
     */
    public Order getOrderById(String id, User currentUser) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Check if user owns this order (or is admin)
        if (!order.getUser().getId().equals(currentUser.getId()) &&
            currentUser.getRole() != Role.ADMIN) {
            throw new AuthException("You don't have permission to view this order");
        }

        return order;
    }

    /**
     * Update order status (Admin only)
     */
    @Transactional
    public Order updateOrderStatus(String id, OrderStatus status, String note, User admin) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setStatus(status);
        order.addStatusHistory(status.name(), note != null ? note : "Status changed to " + status, admin);

        // Auto actions based on status
        if (status == OrderStatus.Delivered) {
            order.setDeliveredAt(LocalDateTime.now());
            order.setPaymentStatus(PaymentStatus.Paid);
            order.setPaymentDate(LocalDateTime.now());
        } else if (status == OrderStatus.Cancelled) {
            order.setCancelledAt(LocalDateTime.now());
            order.setCancelReason(note);

            // Restore stock
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.increaseStock(item.getQuantity());
                productRepository.save(product);
            }
        }

        return orderRepository.save(order);
    }

    /**
     * Update payment status (Admin only)
     */
    @Transactional
    public Order updatePaymentStatus(String id, PaymentStatus paymentStatus, String transactionId, User admin) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setPaymentStatus(paymentStatus);
        if (paymentStatus == PaymentStatus.Paid) {
            order.setPaymentDate(LocalDateTime.now());
        }
        if (transactionId != null) {
            order.setTransactionId(transactionId);
        }

        return orderRepository.save(order);
    }

    /**
     * Cancel order (User can cancel if status is Pending/Confirmed)
     */
    @Transactional
    public Order cancelOrder(String id, String reason, User user) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Check ownership
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AuthException("You don't have permission to cancel this order");
        }

        // Check if order can be cancelled
        if (!order.canBeCancelled()) {
            throw new IllegalArgumentException("Order cannot be cancelled at this stage");
        }

        order.setStatus(OrderStatus.Cancelled);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(reason);
        order.addStatusHistory(OrderStatus.Cancelled.name(), reason, user);

        // Restore stock
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.increaseStock(item.getQuantity());
            productRepository.save(product);
        }

        return orderRepository.save(order);
    }

    /**
     * Get all orders (Admin only)
     */
    public Page<Order> getAllOrders(Pageable pageable, OrderStatus status, PaymentStatus paymentStatus) {
        if (status != null && paymentStatus != null) {
            return orderRepository.findByStatusAndPaymentStatus(status, paymentStatus, pageable);
        } else if (status != null) {
            return orderRepository.findByStatus(status, pageable);
        } else if (paymentStatus != null) {
            return orderRepository.findByPaymentStatus(paymentStatus, pageable);
        }
        return orderRepository.findAll(pageable);
    }
}

