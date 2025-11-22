package com.social.back_java.service.impl;

import com.social.back_java.model.*;
import com.social.back_java.repository.CartRepository;
import com.social.back_java.repository.OrderRepository;
import com.social.back_java.repository.ProductRepository;
import com.social.back_java.repository.UserRepository;
import com.social.back_java.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public Optional<Order> getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return orderRepository.findByUser(user);
    }

    @Override
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    @Override
    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order createOrderFromCart(Long userId, ShippingAddress shippingAddress, String paymentMethod, Double discount, String notes) {
        // Validate shipping address
        if (shippingAddress == null || shippingAddress.getFullName() == null || 
            shippingAddress.getPhone() == null || shippingAddress.getStreet() == null || 
            shippingAddress.getCity() == null) {
            throw new RuntimeException("Complete shipping address is required (fullName, phone, street, city)");
        }

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Get user's cart
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Verify stock availability and prepare order items
        List<OrderItem> orderItems = new ArrayList<>();
        double subtotal = 0.0;

        for (CartItem cartItem : cart.getItems()) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product " + cartItem.getProductSnapshot().getName() + " no longer exists"));

            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for " + product.getName() + ". Only " + product.getStock() + " available");
            }

            // Use discounted price if available
            Double finalPrice = product.getPricegiamgia() != null ? product.getPricegiamgia() : product.getPrice();
            double itemSubtotal = finalPrice * cartItem.getQuantity();
            subtotal += itemSubtotal;

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setName(product.getName());
            orderItem.setImage(product.getImages().isEmpty() ? null : product.getImages().get(0).getUrl());
            orderItem.setCategory(product.getCategory());
            orderItem.setPrice(finalPrice);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setSubtotal(itemSubtotal);

            orderItems.add(orderItem);
        }

        // Calculate tax (10%)
        double tax = subtotal * 0.1;

        // Calculate total
        double discountAmount = discount != null ? discount : 0.0;
        double total = subtotal + tax - discountAmount;

        // Generate order number
        long count = orderRepository.count();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String dateStr = dateFormat.format(new Date());
        String orderNumber = String.format("ORD%s%05d", dateStr, count + 1);

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(orderNumber);
        order.setItems(orderItems);
        order.setSubtotal(subtotal);
        order.setTax(tax);
        order.setDiscount(discountAmount);
        order.setTotal(total);
        order.setShippingAddress(shippingAddress);
        order.setPaymentMethod(paymentMethod != null ? paymentMethod : "COD");
        order.setNotes(notes);
        order.setStatus("Pending");

        // Add initial status history
        OrderStatusHistory statusHistory = new OrderStatusHistory();
        statusHistory.setStatus("Pending");
        statusHistory.setNote("Order created");
        statusHistory.setTimestamp(new Date());
        order.getStatusHistory().add(statusHistory);

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Update product stock
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // Clear cart after successful order
        cart.getItems().clear();
        cartRepository.save(cart);

        return savedOrder;
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        order.setStatus(status);

        // Add to status history
        OrderStatusHistory statusHistory = new OrderStatusHistory();
        statusHistory.setStatus(status);
        statusHistory.setNote("Status changed to " + status);
        statusHistory.setTimestamp(new Date());
        order.getStatusHistory().add(statusHistory);

        // Update specific timestamps
        if ("Delivered".equals(status)) {
            order.setDeliveredAt(new Date());
            order.setPaymentStatus("Paid");
            order.setPaymentDate(new Date());
        }

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order cancelOrder(Long id, String reason) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        // Check if order can be cancelled
        if (!"Pending".equals(order.getStatus()) && !"Confirmed".equals(order.getStatus())) {
            throw new RuntimeException("Order cannot be cancelled at this stage");
        }
        
        order.setStatus("Cancelled");
        order.setCancelReason(reason != null ? reason : "Cancelled by user");
        order.setCancelledAt(new Date());

        // Add to status history
        OrderStatusHistory statusHistory = new OrderStatusHistory();
        statusHistory.setStatus("Cancelled");
        statusHistory.setNote(order.getCancelReason());
        statusHistory.setTimestamp(new Date());
        order.getStatusHistory().add(statusHistory);

        // Restore stock
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElse(null);
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }

        return orderRepository.save(order);
    }
}
