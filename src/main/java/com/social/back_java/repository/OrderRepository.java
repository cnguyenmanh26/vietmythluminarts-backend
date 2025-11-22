package com.social.back_java.repository;

import com.social.back_java.model.Order;
import com.social.back_java.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByStatus(String status);
}
