package com.social.vietmythluminartsbackend.controller;

import com.social.vietmythluminartsbackend.dto.request.ChangePasswordRequest;
import com.social.vietmythluminartsbackend.dto.request.UpdateProfileRequest;
import com.social.vietmythluminartsbackend.dto.response.ApiResponse;
import com.social.vietmythluminartsbackend.dto.response.UserResponse;
import com.social.vietmythluminartsbackend.service.AuthService;
import com.social.vietmythluminartsbackend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller for user management endpoints
 * Requires authentication
 * 
 * Base URL: /api/users
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    /**
     * Get current user profile
     * GET /api/users/me
     * 
     * @param userDetails Authenticated user details
     * @return Current user information
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        var user = userService.findByEmail(email);
        UserResponse userResponse = userService.getUserById(user.getId());
        
        ApiResponse<UserResponse> response = ApiResponse.success(userResponse);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update current user profile
     * PUT /api/users/me
     * 
     * @param request Update profile request body
     * @param userDetails Authenticated user details
     * @return Updated user information
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        var user = userService.findByEmail(email);
        
        UserResponse userResponse = userService.updateProfile(user.getId(), request);
        
        ApiResponse<UserResponse> response = ApiResponse.success(
                "Profile updated successfully", 
                userResponse
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update user avatar
     * POST /api/users/me/avatar
     * 
     * @param file Avatar image file
     * @param userDetails Authenticated user details
     * @return Updated user information with new avatar
     */
    @PostMapping("/me/avatar")
    public ResponseEntity<ApiResponse<UserResponse>> updateAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        var user = userService.findByEmail(email);
        
        UserResponse userResponse = userService.updateAvatar(user.getId(), file);
        
        ApiResponse<UserResponse> response = ApiResponse.success(
                "Avatar updated successfully", 
                userResponse
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Change password
     * PUT /api/users/me/password
     * 
     * @param request Change password request body
     * @param userDetails Authenticated user details
     * @return Success message
     */
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        var user = userService.findByEmail(email);
        
        authService.changePassword(user.getId(), request);
        
        ApiResponse<Void> response = ApiResponse.success(
                "Password changed successfully", 
                null
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Logout from all devices
     * POST /api/users/me/logout-all
     * 
     * @param userDetails Authenticated user details
     * @param httpRequest HTTP request for IP extraction
     * @return Success message
     */
    @PostMapping("/me/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAllDevices(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        String email = userDetails.getUsername();
        var user = userService.findByEmail(email);
        
        String ipAddress = getClientIp(httpRequest);
        authService.logoutAllDevices(user.getId(), ipAddress);
        
        ApiResponse<Void> response = ApiResponse.success(
                "Logged out from all devices successfully", 
                null
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate account
     * DELETE /api/users/me
     * 
     * @param userDetails Authenticated user details
     * @return Success message
     */
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deactivateAccount(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        var user = userService.findByEmail(email);
        
        userService.deactivateAccount(user.getId());
        
        ApiResponse<Void> response = ApiResponse.success(
                "Account deactivated successfully", 
                null
        );
        
        return ResponseEntity.ok(response);
    }

    // ==================== Helper Methods ====================

    /**
     * Extract client IP address from request
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}

