package com.social.vietmythluminartsbackend.service;

import com.social.vietmythluminartsbackend.exception.TokenException;
import com.social.vietmythluminartsbackend.model.RefreshToken;
import com.social.vietmythluminartsbackend.model.User;
import com.social.vietmythluminartsbackend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

/**
 * Service for managing refresh tokens
 * Handles token creation, validation, rotation, and revocation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token.expiration}")
    private Long refreshTokenExpiration; // in milliseconds

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    /**
     * Generate a new refresh token for user
     * @param user User entity
     * @param ipAddress Client IP address
     * @return Created RefreshToken
     */
    @Transactional
    public RefreshToken createRefreshToken(User user, String ipAddress) {
        // Generate random token string
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);
        String tokenString = base64Encoder.encodeToString(randomBytes);

        // Calculate expiration time
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(refreshTokenExpiration / 1000);

        // Create and save token
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenString)
                .user(user)
                .expiresAt(expiresAt)
                .createdByIp(ipAddress)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Find refresh token by token string
     * @param token Token string
     * @return RefreshToken entity
     * @throws TokenException if token not found
     */
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenException("Refresh token not found"));
    }

    /**
     * Validate refresh token
     * @param token Token string
     * @return RefreshToken if valid
     * @throws TokenException if token is invalid
     */
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = findByToken(token);

        if (!refreshToken.isActive()) {
            throw new TokenException("Refresh token has been revoked");
        }

        if (refreshToken.isExpired()) {
            throw new TokenException("Refresh token has expired");
        }

        return refreshToken;
    }

    /**
     * Rotate refresh token (revoke old and create new)
     * @param oldToken Old token string
     * @param ipAddress Client IP address
     * @return New RefreshToken
     */
    @Transactional
    public RefreshToken rotateRefreshToken(String oldToken, String ipAddress) {
        RefreshToken oldRefreshToken = validateRefreshToken(oldToken);
        User user = oldRefreshToken.getUser();

        // Create new token
        RefreshToken newRefreshToken = createRefreshToken(user, ipAddress);

        // Revoke old token
        oldRefreshToken.revoke(ipAddress, newRefreshToken.getToken());
        refreshTokenRepository.save(oldRefreshToken);

        log.info("Rotated refresh token for user: {}", user.getEmail());
        return newRefreshToken;
    }

    /**
     * Revoke a single refresh token
     * @param token Token string
     * @param ipAddress Client IP address
     */
    @Transactional
    public void revokeToken(String token, String ipAddress) {
        RefreshToken refreshToken = findByToken(token);
        refreshToken.revoke(ipAddress, null);
        refreshTokenRepository.save(refreshToken);
        log.info("Revoked refresh token for user: {}", refreshToken.getUser().getEmail());
    }

    /**
     * Revoke all tokens for a user
     * @param user User entity
     * @param ipAddress Client IP address
     */
    @Transactional
    public void revokeAllUserTokens(User user, String ipAddress) {
        List<RefreshToken> activeTokens = refreshTokenRepository
                .findByUserAndRevokedAtIsNull(user);

        for (RefreshToken token : activeTokens) {
            token.revoke(ipAddress, null);
            refreshTokenRepository.save(token);
        }

        log.info("Revoked all tokens for user: {}", user.getEmail());
    }

    /**
     * Delete all tokens for a user (complete logout)
     * @param user User entity
     */
    @Transactional
    public void deleteAllUserTokens(User user) {
        refreshTokenRepository.deleteByUser(user);
        log.info("Deleted all tokens for user: {}", user.getEmail());
    }

    /**
     * Clean up expired tokens (scheduled task)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("Cleaned up expired refresh tokens");
    }

    /**
     * Get count of active tokens for user
     * @param user User entity
     * @return Number of active tokens
     */
    public long countActiveTokens(User user) {
        return refreshTokenRepository.countByUserAndRevokedAtIsNull(user);
    }
}

