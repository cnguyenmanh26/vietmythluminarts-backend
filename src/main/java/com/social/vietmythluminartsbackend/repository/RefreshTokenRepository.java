package com.social.vietmythluminartsbackend.repository;

import com.social.vietmythluminartsbackend.model.RefreshToken;
import com.social.vietmythluminartsbackend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for RefreshToken entity
 * Handles token storage and retrieval
 */
@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {

    /**
     * Find refresh token by token string
     * @param token The refresh token string
     * @return Optional containing RefreshToken if found
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find all tokens for a specific user
     * @param user The user
     * @return List of refresh tokens belonging to the user
     */
    List<RefreshToken> findByUser(User user);

    /**
     * Find all active tokens for a user
     * @param user The user
     * @return List of active (not revoked) tokens
     */
    List<RefreshToken> findByUserAndRevokedAtIsNull(User user);

    /**
     * Delete all tokens for a user
     * @param user The user
     */
    void deleteByUser(User user);

    /**
     * Delete all revoked tokens
     */
    void deleteByRevokedAtIsNotNull();

    /**
     * Delete expired tokens
     * @param now Current time
     */
    void deleteByExpiresAtBefore(LocalDateTime now);

    /**
     * Count active tokens for a user
     * @param user The user
     * @return Number of active tokens
     */
    long countByUserAndRevokedAtIsNull(User user);
}

