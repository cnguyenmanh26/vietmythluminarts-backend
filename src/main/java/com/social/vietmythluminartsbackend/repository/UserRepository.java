package com.social.vietmythluminartsbackend.repository;

import com.social.vietmythluminartsbackend.model.User;
import com.social.vietmythluminartsbackend.model.enums.Role;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity
 * Spring Data MongoDB will generate implementation automatically
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * Find user by email address
     * @param email User's email
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by Google ID
     * @param googleId Google OAuth ID
     * @return Optional containing user if found
     */
    Optional<User> findByGoogleId(String googleId);

    /**
     * Find user by phone number
     * @param phoneNumber User's phone number
     * @return Optional containing user if found
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * Find user by password reset token
     * @param resetPasswordToken The reset token
     * @return Optional containing user if found
     */
    Optional<User> findByResetPasswordToken(String resetPasswordToken);

    /**
     * Check if email already exists
     * @param email Email to check
     * @return true if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if phone number already exists
     * @param phoneNumber Phone number to check
     * @return true if phone number exists
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Check if Google ID already exists
     * @param googleId Google ID to check
     * @return true if Google ID exists
     */
    boolean existsByGoogleId(String googleId);

    /**
     * Find all users by role
     * @param role User role
     * @return List of users with the specified role
     */
    List<User> findByRole(Role role);

    /**
     * Find all active users
     * @param isActive Active status
     * @return List of users with the specified active status
     */
    List<User> findByIsActive(Boolean isActive);

    /**
     * Count users by role
     * @param role User role
     * @return Number of users with the role
     */
    long countByRole(Role role);

    /**
     * Find users by role and active status
     */
    List<User> findByRoleAndIsActive(Role role, Boolean isActive);

    /**
     * Find users by name or email (case-insensitive search)
     */
    List<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
}

