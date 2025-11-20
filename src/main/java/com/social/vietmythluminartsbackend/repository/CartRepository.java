package com.social.vietmythluminartsbackend.repository;

import com.social.vietmythluminartsbackend.model.Cart;
import com.social.vietmythluminartsbackend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Cart entity
 */
@Repository
public interface CartRepository extends MongoRepository<Cart, String> {

    /**
     * Find cart by user
     */
    Optional<Cart> findByUser(User user);

    /**
     * Find cart by user ID
     */
    Optional<Cart> findByUserId(String userId);

    /**
     * Delete cart by user
     */
    void deleteByUser(User user);
}

