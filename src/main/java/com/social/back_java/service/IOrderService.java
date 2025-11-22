package com.social.back_java.service;

import com.social.back_java.model.Order;
import com.social.back_java.model.ShippingAddress;

import java.util.List;
import java.util.Optional;

public interface IOrderService {
    List<Order> getAllOrders();
    Optional<Order> getOrderById(Long id);
    Optional<Order> getOrderByOrderNumber(String orderNumber);
    List<Order> getOrdersByUserId(Long userId);
    List<Order> getOrdersByStatus(String status);
    Order createOrder(Order order);
    Order createOrderFromCart(Long userId, ShippingAddress shippingAddress, String paymentMethod, Double discount, String notes);
    Order updateOrderStatus(Long id, String status);
    Order cancelOrder(Long id, String reason);
}
