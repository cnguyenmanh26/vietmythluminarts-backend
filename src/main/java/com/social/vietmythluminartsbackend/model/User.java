package com.social.vietmythluminartsbackend.model;

import com.social.vietmythluminartsbackend.model.embedded.Address;
import com.social.vietmythluminartsbackend.model.enums.Gender;
import com.social.vietmythluminartsbackend.model.enums.Provider;
import com.social.vietmythluminartsbackend.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * User entity representing a user in the system
 * Migrated from Node.js User model (WoodToy backend)
 * 
 * Features:
 * - Local authentication (email/password)
 * - Google OAuth authentication
 * - Role-based access control (USER, ADMIN)
 * - Account activation/deactivation
 * - Password reset functionality
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    /**
     * User's full name
     * Required field, must be 2-50 characters
     * Can only contain letters and spaces
     */
    private String name;

    /**
     * User's email address
     * Unique, indexed, and required for local accounts
     * Optional for OAuth-only accounts
     */
    @Indexed(unique = true, sparse = true)
    private String email;

    /**
     * User's phone number (Vietnamese format)
     * Optional field
     * Format: 0912345678 or +84912345678
     */
    @Indexed(unique = true, sparse = true)
    private String phoneNumber;

    /**
     * Hashed password for local authentication
     * Required for local accounts, null for OAuth accounts
     * Minimum 8 characters with letters, numbers, and special chars
     */
    private String password;

    /**
     * Google OAuth ID
     * Unique identifier from Google
     * Only present for users who authenticated via Google
     */
    @Indexed(unique = true, sparse = true)
    private String googleId;

    /**
     * User's profile avatar URL
     * Can be from Google or uploaded to Cloudinary
     */
    private String avatar;

    /**
     * User's gender
     * Options: MALE, FEMALE, OTHER
     */
    private Gender gender;

    /**
     * User's shipping/billing address
     * Embedded document containing street, ward, district, city, etc.
     */
    private Address address;

    /**
     * Authentication provider
     * LOCAL: Email/password authentication
     * GOOGLE: Google OAuth authentication
     */
    @Builder.Default
    private Provider provider = Provider.LOCAL;

    /**
     * User's role in the system
     * USER: Regular user (default)
     * ADMIN: Administrator with full access
     */
    @Builder.Default
    private Role role = Role.USER;

    /**
     * Account activation status
     * False when account is deactivated by admin
     * Deactivated users cannot login
     */
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Password reset token (hashed)
     * Used for forgot password functionality
     * Null when no reset is in progress
     */
    private String resetPasswordToken;

    /**
     * Password reset token expiration
     * Token is valid for 10 minutes after generation
     */
    private LocalDateTime resetPasswordExpire;

    /**
     * Last successful login timestamp
     * Updated on every successful login
     */
    private LocalDateTime lastLogin;

    /**
     * Account creation timestamp
     * Automatically set by Spring Data MongoDB
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     * Automatically updated by Spring Data MongoDB
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ==================== Business Logic Methods ====================

    /**
     * Check if user is an admin
     * @return true if user has ADMIN role
     */
    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    /**
     * Check if user uses local authentication
     * @return true if provider is LOCAL
     */
    public boolean isLocalProvider() {
        return this.provider == Provider.LOCAL;
    }

    /**
     * Check if user uses Google OAuth
     * @return true if provider is GOOGLE
     */
    public boolean isGoogleProvider() {
        return this.provider == Provider.GOOGLE;
    }

    /**
     * Check if password reset token is still valid
     * @return true if token exists and hasn't expired
     */
    public boolean isResetTokenValid() {
        return this.resetPasswordToken != null 
            && this.resetPasswordExpire != null 
            && this.resetPasswordExpire.isAfter(LocalDateTime.now());
    }

    /**
     * Clear password reset token and expiration
     * Called after successful password reset
     */
    public void clearResetToken() {
        this.resetPasswordToken = null;
        this.resetPasswordExpire = null;
    }
}

