package com.social.vietmythluminartsbackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Refresh Token entity for JWT token rotation strategy
 * Migrated from Node.js RefreshToken model
 * 
 * Features:
 * - Secure token storage with auto-expiration
 * - Token revocation support
 * - IP address tracking
 * - Token replacement chain
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "refreshtokens")
public class RefreshToken {

    @Id
    private String id;

    /**
     * The actual refresh token string (random crypto bytes)
     * Indexed for fast lookup during token refresh
     */
    @Indexed(unique = true)
    private String token;

    /**
     * Reference to the user who owns this token
     * DBRef for relationship with User collection
     */
    @DBRef
    private User user;

    /**
     * Token expiration timestamp
     * Indexed with TTL for automatic deletion
     * Tokens typically expire after 7 days
     */
    @Indexed(expireAfterSeconds = 0) // TTL index - expires at this time
    private LocalDateTime expiresAt;

    /**
     * IP address from which token was created
     * Used for security tracking
     */
    private String createdByIp;

    /**
     * Timestamp when token was revoked
     * Null if token is still active
     */
    private LocalDateTime revokedAt;

    /**
     * IP address from which token was revoked
     * Used for security audit
     */
    private String revokedByIp;

    /**
     * The token that replaced this one
     * Used to maintain token rotation chain
     */
    private String replacedByToken;

    /**
     * Token creation timestamp
     * Automatically set by Spring Data MongoDB
     */
    @CreatedDate
    private LocalDateTime createdAt;

    // ==================== Business Logic Methods ====================

    /**
     * Check if token is currently active (not revoked)
     * @return true if token hasn't been revoked
     */
    public boolean isActive() {
        return this.revokedAt == null;
    }

    /**
     * Check if token has expired
     * @return true if current time is past expiration
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * Check if token is valid (active and not expired)
     * @return true if token can be used
     */
    public boolean isValid() {
        return isActive() && !isExpired();
    }

    /**
     * Revoke this token
     * @param ipAddress IP address of the revocation request
     * @param replacementToken Optional token that replaces this one
     */
    public void revoke(String ipAddress, String replacementToken) {
        this.revokedAt = LocalDateTime.now();
        this.revokedByIp = ipAddress;
        this.replacedByToken = replacementToken;
    }
}

