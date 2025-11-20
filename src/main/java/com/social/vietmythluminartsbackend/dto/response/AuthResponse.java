package com.social.vietmythluminartsbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for authentication operations (login, register)
 * Contains tokens and user information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /**
     * JWT access token
     * Short-lived token (15-60 minutes) for API authentication
     */
    private String accessToken;

    /**
     * Refresh token
     * Long-lived token (7-30 days) for obtaining new access tokens
     */
    private String refreshToken;

    /**
     * Token type (always "Bearer")
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * User information
     */
    private UserResponse user;
}

