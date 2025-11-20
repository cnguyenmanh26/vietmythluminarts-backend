package com.social.vietmythluminartsbackend.service;

import com.social.vietmythluminartsbackend.dto.request.*;
import com.social.vietmythluminartsbackend.dto.response.AuthResponse;
import com.social.vietmythluminartsbackend.dto.response.UserResponse;
import com.social.vietmythluminartsbackend.exception.AuthException;
import com.social.vietmythluminartsbackend.exception.ResourceNotFoundException;
import com.social.vietmythluminartsbackend.model.RefreshToken;
import com.social.vietmythluminartsbackend.model.User;
import com.social.vietmythluminartsbackend.model.enums.Provider;
import com.social.vietmythluminartsbackend.model.enums.Role;
import com.social.vietmythluminartsbackend.repository.UserRepository;
import com.social.vietmythluminartsbackend.util.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service handling authentication operations
 * Migrated from Node.js authController.js
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final GoogleAuthService googleAuthService;

    /**
     * Register a new user with local authentication
     * @param request Registration request
     * @param ipAddress Client IP address
     * @return Authentication response with tokens
     */
    @Transactional
    public AuthResponse register(RegisterRequest request, String ipAddress) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Email already registered");
        }

        // Check if phone number already exists (if provided)
        if (request.getPhoneNumber() != null && 
            userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new AuthException("Phone number already registered");
        }

        // Create new user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .gender(request.getGender())
                .address(request.getAddress())
                .provider(Provider.LOCAL)
                .role(Role.USER)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getEmail());

        // Send welcome email
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getName());

        // Generate tokens
        return generateAuthResponse(savedUser, ipAddress);
    }

    /**
     * Login with email and password
     * @param request Login request
     * @param ipAddress Client IP address
     * @return Authentication response with tokens
     */
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Get user details
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("Invalid credentials"));

        // Check if account is active
        if (!user.getIsActive()) {
            throw new AuthException("Account has been deactivated. Please contact administrator.");
        }

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        log.info("User logged in: {}", user.getEmail());

        // Generate tokens
        return generateAuthResponse(user, ipAddress);
    }

    /**
     * Authenticate with Google OAuth
     * @param idToken Google ID token
     * @param ipAddress Client IP address
     * @return Authentication response with tokens
     */
    @Transactional
    public AuthResponse authenticateWithGoogle(String idToken, String ipAddress) {
        // Verify Google token
        GoogleAuthService.GoogleUserInfo googleUser = googleAuthService.verifyToken(idToken);

        if (googleUser == null) {
            throw new AuthException("Invalid Google token");
        }

        // Check if email is verified
        if (!Boolean.TRUE.equals(googleUser.getEmailVerified())) {
            throw new AuthException("Please verify your Google email first");
        }

        // Check if user exists with this Google ID
        User user = userRepository.findByGoogleId(googleUser.getGoogleId())
                .orElse(null);

        // If not found by Google ID, check by email
        if (user == null) {
            user = userRepository.findByEmail(googleUser.getEmail()).orElse(null);
        }

        if (user != null) {
            // User exists - update Google info if needed
            boolean updated = false;
            
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleUser.getGoogleId());
                updated = true;
            }
            if (user.getAvatar() == null && googleUser.getAvatar() != null) {
                user.setAvatar(googleUser.getAvatar());
                updated = true;
            }
            if (user.getName() == null && googleUser.getName() != null) {
                user.setName(googleUser.getName());
                updated = true;
            }
            if (user.getProvider() == Provider.LOCAL) {
                // Link Google account to existing local account
                user.setProvider(Provider.GOOGLE);
                updated = true;
            }

            if (updated) {
                userRepository.save(user);
            }
        } else {
            // Create new user with Google account
            String userName = googleUser.getName();
            if (userName == null || userName.isEmpty()) {
                // Extract from email and sanitize
                String emailPrefix = googleUser.getEmail().split("@")[0];
                userName = emailPrefix.replaceAll("[^a-zA-Z]", "");
                if (userName.isEmpty()) {
                    userName = "User";
                }
            }

            user = User.builder()
                    .name(userName)
                    .email(googleUser.getEmail())
                    .googleId(googleUser.getGoogleId())
                    .avatar(googleUser.getAvatar())
                    .provider(Provider.GOOGLE)
                    .role(Role.USER)
                    .isActive(true)
                    .lastLogin(LocalDateTime.now())
                    .build();

            user = userRepository.save(user);
            log.info("New user created via Google OAuth: {}", user.getEmail());

            // Send welcome email
            emailService.sendWelcomeEmail(user.getEmail(), user.getName());
        }

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Generate tokens
        return generateAuthResponse(user, ipAddress);
    }

    /**
     * Refresh access token using refresh token
     * @param request Refresh token request
     * @param ipAddress Client IP address
     * @return New authentication response
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request, String ipAddress) {
        // Validate and rotate refresh token
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(
                request.getRefreshToken(), 
                ipAddress
        );

        User user = newRefreshToken.getUser();

        // Check if account is still active
        if (!user.getIsActive()) {
            throw new AuthException("Account has been deactivated");
        }

        // Generate new access token
        String accessToken = generateAccessToken(user);

        UserResponse userResponse = userMapper.toResponse(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken.getToken())
                .user(userResponse)
                .build();
    }

    /**
     * Logout user (revoke refresh token)
     * @param refreshToken Refresh token to revoke
     * @param ipAddress Client IP address
     */
    @Transactional
    public void logout(String refreshToken, String ipAddress) {
        refreshTokenService.revokeToken(refreshToken, ipAddress);
        log.info("User logged out");
    }

    /**
     * Logout from all devices (revoke all tokens)
     * @param userId User ID
     * @param ipAddress Client IP address
     */
    @Transactional
    public void logoutAllDevices(String userId, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        refreshTokenService.revokeAllUserTokens(user, ipAddress);
        log.info("User logged out from all devices: {}", user.getEmail());
    }

    /**
     * Send password reset email
     * @param request Forgot password request
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No account found with this email"));

        // Check if user uses local authentication
        if (user.getProvider() != Provider.LOCAL) {
            throw new AuthException(
                    "This account uses " + user.getProvider() + " login. " +
                    "Please use " + user.getProvider() + " to sign in."
            );
        }

        // Generate reset token (UUID)
        String resetToken = UUID.randomUUID().toString();
        String hashedToken = passwordEncoder.encode(resetToken);

        // Set token and expiration (10 minutes)
        user.setResetPasswordToken(hashedToken);
        user.setResetPasswordExpire(LocalDateTime.now().plusMinutes(10));

        userRepository.save(user);

        // Send email with reset link
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);

        log.info("Password reset token generated for: {}", user.getEmail());
    }

    /**
     * Reset password using token
     * @param token Reset token
     * @param request Reset password request
     */
    @Transactional
    public void resetPassword(String token, ResetPasswordRequest request) {
        // Find user by token (need to check all users due to hashed tokens)
        User user = userRepository.findAll().stream()
                .filter(u -> u.getResetPasswordToken() != null &&
                        passwordEncoder.matches(token, u.getResetPasswordToken()))
                .findFirst()
                .orElseThrow(() -> new AuthException("Invalid or expired reset token"));

        // Check if token is expired
        if (!user.isResetTokenValid()) {
            throw new AuthException("Reset token has expired");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.clearResetToken();

        userRepository.save(user);

        // Revoke all existing refresh tokens for security
        refreshTokenService.deleteAllUserTokens(user);

        log.info("Password reset successful for: {}", user.getEmail());
    }

    /**
     * Change password for authenticated user
     * @param userId User ID
     * @param request Change password request
     */
    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user uses local authentication
        if (user.getProvider() != Provider.LOCAL) {
            throw new AuthException(
                    "Cannot change password for " + user.getProvider() + " accounts"
            );
        }

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AuthException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed for user: {}", user.getEmail());
    }

    // ==================== Helper Methods ====================

    /**
     * Generate complete auth response with tokens
     * @param user User entity
     * @param ipAddress Client IP address
     * @return AuthResponse
     */
    private AuthResponse generateAuthResponse(User user, String ipAddress) {
        // Generate access token
        String accessToken = generateAccessToken(user);

        // Generate refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, ipAddress);

        // Map user to response
        UserResponse userResponse = userMapper.toResponse(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .user(userResponse)
                .build();
    }

    /**
     * Generate JWT access token for user
     * @param user User entity
     * @return JWT token string
     */
    private String generateAccessToken(User user) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();

        return jwtService.generateToken(userDetails, user.getId(), user.getRole().name());
    }
}
