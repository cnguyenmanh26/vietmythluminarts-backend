package com.social.vietmythluminartsbackend.controller;

import com.social.vietmythluminartsbackend.dto.request.*;
import com.social.vietmythluminartsbackend.dto.response.ApiResponse;
import com.social.vietmythluminartsbackend.dto.response.AuthResponse;
import com.social.vietmythluminartsbackend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication endpoints
 * Migrated from Node.js authController.js
 * 
 * Base URL: /api/auth
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user
     * POST /api/auth/register
     * 
     * @param request Registration request body
     * @param httpRequest HTTP request for IP extraction
     * @return AuthResponse with tokens and user info
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        String ipAddress = getClientIp(httpRequest);
        AuthResponse authResponse = authService.register(request, ipAddress);
        
        ApiResponse<AuthResponse> response = ApiResponse.success(
                "Registration successful", 
                authResponse
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Login with email and password
     * POST /api/auth/login
     * 
     * @param request Login request body
     * @param httpRequest HTTP request for IP extraction
     * @return AuthResponse with tokens and user info
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        String ipAddress = getClientIp(httpRequest);
        AuthResponse authResponse = authService.login(request, ipAddress);
        
        ApiResponse<AuthResponse> response = ApiResponse.success(
                "Login successful", 
                authResponse
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token
     * POST /api/auth/refresh-token
     * 
     * @param request Refresh token request body
     * @param httpRequest HTTP request for IP extraction
     * @return New AuthResponse with refreshed tokens
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest
    ) {
        String ipAddress = getClientIp(httpRequest);
        AuthResponse authResponse = authService.refreshToken(request, ipAddress);
        
        ApiResponse<AuthResponse> response = ApiResponse.success(
                "Token refreshed successfully", 
                authResponse
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Logout (revoke refresh token)
     * POST /api/auth/logout
     * 
     * @param request Refresh token request body
     * @param httpRequest HTTP request for IP extraction
     * @return Success message
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest
    ) {
        String ipAddress = getClientIp(httpRequest);
        authService.logout(request.getRefreshToken(), ipAddress);
        
        ApiResponse<Void> response = ApiResponse.success(
                "Logout successful", 
                null
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Forgot password - Send reset email
     * POST /api/auth/forgot-password
     * 
     * @param request Forgot password request body
     * @return Success message
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        authService.forgotPassword(request);
        
        ApiResponse<Void> response = ApiResponse.success(
                "Password reset instructions have been sent to your email", 
                null
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Reset password with token
     * PUT /api/auth/reset-password/{token}
     * 
     * @param token Reset token from email
     * @param request Reset password request body
     * @return Success message
     */
    @PutMapping("/reset-password/{token}")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable String token,
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        authService.resetPassword(token, request);
        
        ApiResponse<Void> response = ApiResponse.success(
                "Password has been reset successfully. Please login with your new password.", 
                null
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Google OAuth authentication
     * POST /api/auth/google
     * 
     * @param request Google auth request with token
     * @param httpRequest HTTP request for IP extraction
     * @return AuthResponse with tokens and user info
     */
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> googleAuth(
            @Valid @RequestBody GoogleAuthRequest request,
            HttpServletRequest httpRequest
    ) {
        String ipAddress = getClientIp(httpRequest);
        AuthResponse authResponse = authService.authenticateWithGoogle(request.getToken(), ipAddress);
        
        ApiResponse<AuthResponse> response = ApiResponse.success(
                "Google authentication successful",
                authResponse
        );
        
        return ResponseEntity.ok(response);
    }

    // ==================== Helper Methods ====================

    /**
     * Extract client IP address from request
     * Handles proxy headers (X-Forwarded-For, X-Real-IP)
     * 
     * @param request HTTP request
     * @return Client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // Handle multiple IPs in X-Forwarded-For (use first one)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}

