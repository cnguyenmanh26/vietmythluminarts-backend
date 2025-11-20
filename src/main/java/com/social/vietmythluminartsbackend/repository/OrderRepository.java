package com.social.vietmythluminartsbackend.repository;

import com.social.vietmythluminartsbackend.model.Order;
import com.social.vietmythluminartsbackend.model.User;
import com.social.vietmythluminartsbackend.model.enums.OrderStatus;
import com.social.vietmythluminartsbackend.model.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Order entity
 */
@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    /**
     * Find order by order number
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Find orders by user
     */
    Page<Order> findByUser(User user, Pageable pageable);

    /**
     * Find orders by user and status
     */
    Page<Order> findByUserAndStatus(User user, OrderStatus status, Pageable pageable);

    /**
     * Find all orders by status
     */
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    /**
     * Find all orders by payment status
     */
    Page<Order> findByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable);

    /**
     * Find orders by status and payment status
     */
    Page<Order> findByStatusAndPaymentStatus(OrderStatus status, PaymentStatus paymentStatus, Pageable pageable);
}

