package com.social.back_java.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.social.back_java.dto.auth.*;
import com.social.back_java.model.RefreshToken;
import com.social.back_java.model.Role;
import com.social.back_java.model.User;
import com.social.back_java.repository.UserRepository;
import com.social.back_java.service.ICloudinaryService;
import com.social.back_java.service.IRefreshTokenService;
import com.social.back_java.util.GoogleAuthUtil;
import com.social.back_java.util.JwtUtil;
import com.social.back_java.util.PasswordUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private IRefreshTokenService refreshTokenService;

    @Autowired
    private GoogleAuthUtil googleAuthUtil;

    @Autowired
    private ICloudinaryService cloudinaryService;

    // Email pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    // Password pattern: min 8 chars, 1 letter, 1 number, 1 special char
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    // Name pattern: letters and spaces only
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-ZÀ-ỹ\\s]+$");
    // Vietnamese phone pattern
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+84|0)[3|5|7|8|9][0-9]{8}$");

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getGender(),
                user.getAddress(),
                user.getRole(),
                user.getAvatar(),
                user.getProvider(),
                user.isActive(),
                user.getLastLogin(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        try {
            // Validate required fields
            if (request.getName() == null || request.getEmail() == null || request.getPassword() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Please provide name, email, and password"
                ));
            }

            // Validate name
            if (request.getName().length() < 2 || request.getName().length() > 50) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Name must be between 2 and 50 characters"
                ));
            }
            if (!NAME_PATTERN.matcher(request.getName()).matches()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Name can only contain letters and spaces"
                ));
            }

            // Validate email
            if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Please provide a valid email address"
                ));
            }

            // Validate password
            if (request.getPassword().length() < 8) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Password must be at least 8 characters"
                ));
            }
            if (!PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Password must contain at least 1 letter, 1 number, and 1 special character (@$!%*?&)"
                ));
            }

            // Validate phone if provided
            if (request.getPhoneNumber() != null && !PHONE_PATTERN.matcher(request.getPhoneNumber()).matches()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Please provide a valid Vietnamese phone number"
                ));
            }

            // Validate gender if provided
            if (request.getGender() != null && !request.getGender().matches("(?i)male|female|other")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Gender must be either 'male', 'female', or 'other'"
                ));
            }

            // Check if user exists
            if (userRepository.findByEmail(request.getEmail().toLowerCase()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "User with this email already exists"
                ));
            }

            // Create user
            User user = new User();
            user.setName(request.getName().trim());
            user.setEmail(request.getEmail().toLowerCase().trim());
            user.setPassword(request.getPassword()); // Will be hashed by @PrePersist
            user.setRole(Role.user);
            user.setLastLogin(new Date());
            user.setProvider("local");

            if (request.getPhoneNumber() != null) {
                user.setPhoneNumber(request.getPhoneNumber().trim());
            }
            if (request.getGender() != null) {
                user.setGender(request.getGender().toLowerCase());
            }
            if (request.getAddress() != null) {
                user.setAddress(request.getAddress());
            }

            user = userRepository.save(user);

            // Generate tokens
            String ipAddress = getClientIp(httpRequest);
            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole().name());
            String refreshToken = refreshTokenService.generateRefreshToken(user.getId(), ipAddress);

            AuthResponse response = new AuthResponse(convertToDTO(user), accessToken, refreshToken);

            return ResponseEntity.status(201).body(Map.of(
                    "success", true,
                    "message", "User registered successfully",
                    "data", response
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error registering user",
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            // Validate input
            if (request.getEmail() == null || request.getPassword() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Please provide email and password"
                ));
            }

            // Find user
            User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                    .orElse(null);

            if (user == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Invalid credentials"
                ));
            }

            // Check if active
            if (!user.isActive()) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Account is deactivated"
                ));
            }

            // Verify password
            if (!PasswordUtil.verifyPassword(request.getPassword(), user.getPassword())) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Invalid credentials"
                ));
            }

            // Update last login
            user.setLastLogin(new Date());
            userRepository.save(user);

            // Generate tokens
            String ipAddress = getClientIp(httpRequest);
            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole().name());
            String refreshToken = refreshTokenService.generateRefreshToken(user.getId(), ipAddress);

            AuthResponse response = new AuthResponse(convertToDTO(user), accessToken, refreshToken);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Login successful",
                    "data", response
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error logging in",
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleAuth(@RequestBody GoogleAuthRequest request, HttpServletRequest httpRequest) {
        try {
            if (request.getToken() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Google token is required"
                ));
            }

            // Verify Google token
            GoogleIdToken.Payload payload = googleAuthUtil.verifyGoogleToken(request.getToken());

            if (!payload.getEmailVerified()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Please verify your Google email first"
                ));
            }

            String googleId = payload.getSubject();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String avatar = (String) payload.get("picture");

            // Find or create user
            User user = userRepository.findByGoogleId(googleId).orElse(null);

            if (user == null) {
                user = userRepository.findByEmail(email).orElse(null);
            }

            if (user != null) {
                // Update Google info
                if (user.getGoogleId() == null) {
                    user.setGoogleId(googleId);
                }
                if (user.getAvatar() == null && avatar != null) {
                    user.setAvatar(avatar);
                }
                if (user.getName() == null && name != null) {
                    user.setName(name);
                }
                if ("local".equals(user.getProvider())) {
                    user.setProvider("google");
                }
                user.setLastLogin(new Date());
                userRepository.save(user);
            } else {
                // Create new user
                user = new User();
                user.setName(name != null ? name : email.split("@")[0]);
                user.setEmail(email);
                user.setGoogleId(googleId);
                user.setAvatar(avatar);
                user.setProvider("google");
                user.setRole(Role.user);
                user.setLastLogin(new Date());
                user = userRepository.save(user);
            }

            // Generate tokens
            String ipAddress = getClientIp(httpRequest);
            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole().name());
            String refreshToken = refreshTokenService.generateRefreshToken(user.getId(), ipAddress);

            AuthResponse response = new AuthResponse(convertToDTO(user), accessToken, refreshToken);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Login successful",
                    "data", response
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error with Google authentication",
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        try {
            if (request.getRefreshToken() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Refresh token is required"
                ));
            }

            RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());
            User user = refreshToken.getUser();

            // Generate new tokens
            String ipAddress = getClientIp(httpRequest);
            String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole().name());
            String newRefreshToken = refreshTokenService.generateRefreshToken(user.getId(), ipAddress);

            // Revoke old refresh token
            refreshTokenService.revokeRefreshToken(request.getRefreshToken(), ipAddress, newRefreshToken);

            AuthResponse response = new AuthResponse(convertToDTO(user), newAccessToken, newRefreshToken);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Token refreshed successfully",
                    "data", response
            ));

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Invalid or expired refresh token",
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        try {
            if (request.getRefreshToken() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Refresh token is required"
                ));
            }

            String ipAddress = getClientIp(httpRequest);
            refreshTokenService.revokeRefreshToken(request.getRefreshToken(), ipAddress, null);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Logout successful"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error logging out",
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAll(@AuthenticationPrincipal User user, HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIp(httpRequest);
            refreshTokenService.revokeAllUserTokens(user.getId(), ipAddress);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Logged out from all devices successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error logging out from all devices",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@AuthenticationPrincipal User user) {
        try {
            User fullUser = userRepository.findById(user.getId()).orElseThrow();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", Map.of("user", convertToDTO(fullUser))
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error fetching user profile",
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @RequestPart(required = false) UpdateProfileRequest request,
            @RequestPart(required = false) MultipartFile avatar) {
        try {
            User user = userRepository.findById(currentUser.getId()).orElseThrow();

            // Update name
            if (request != null && request.getName() != null) {
                if (request.getName().length() < 2 || request.getName().length() > 50) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Name must be between 2 and 50 characters"
                    ));
                }
                if (!NAME_PATTERN.matcher(request.getName()).matches()) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Name can only contain letters and spaces"
                    ));
                }
                user.setName(request.getName());
            }

            // Update email
            if (request != null && request.getEmail() != null) {
                if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Please provide a valid email address"
                    ));
                }
                // Check if email is taken
                User existingUser = userRepository.findByEmail(request.getEmail()).orElse(null);
                if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Email already in use"
                    ));
                }
                user.setEmail(request.getEmail());
            }

            // Update phone
            if (request != null && request.getPhoneNumber() != null) {
                if (!request.getPhoneNumber().isEmpty() && !PHONE_PATTERN.matcher(request.getPhoneNumber()).matches()) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Please provide a valid Vietnamese phone number"
                    ));
                }
                user.setPhoneNumber(request.getPhoneNumber().isEmpty() ? null : request.getPhoneNumber());
            }

            // Update gender
            if (request != null && request.getGender() != null) {
                if (!request.getGender().isEmpty() && !request.getGender().matches("(?i)male|female|other")) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Gender must be either 'male', 'female', or 'other'"
                    ));
                }
                user.setGender(request.getGender().isEmpty() ? null : request.getGender().toLowerCase());
            }

            // Update address
            if (request != null && request.getAddress() != null) {
                user.setAddress(request.getAddress());
            }

            // Handle avatar removal
            if (request != null && Boolean.TRUE.equals(request.getRemoveAvatar()) && user.getAvatar() != null) {
                if (!user.getAvatar().contains("googleusercontent.com")) {
                    // Delete from Cloudinary
                    String publicId = extractPublicId(user.getAvatar());
                    if (publicId != null) {
                        try {
                            cloudinaryService.deleteFile(publicId);
                        } catch (IOException ignored) {
                        }
                    }
                }
                user.setAvatar(null);
            }

            // Handle avatar upload
            if (avatar != null && !avatar.isEmpty()) {
                // Delete old avatar if exists
                if (user.getAvatar() != null && !user.getAvatar().contains("googleusercontent.com")) {
                    String oldPublicId = extractPublicId(user.getAvatar());
                    if (oldPublicId != null) {
                        try {
                            cloudinaryService.deleteFile(oldPublicId);
                        } catch (IOException ignored) {
                        }
                    }
                }

                // Upload new avatar
                try {
                    Map<String, Object> uploadResult = cloudinaryService.uploadImage(avatar);
                    user.setAvatar((String) uploadResult.get("secure_url"));
                } catch (IOException e) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Failed to upload avatar: " + e.getMessage()
                    ));
                }
            }

            user = userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Profile updated successfully",
                    "data", Map.of("user", convertToDTO(user))
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error updating profile",
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal User currentUser,
            @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        try {
            if (request.getCurrentPassword() == null || request.getNewPassword() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Please provide current password and new password"
                ));
            }

            User user = userRepository.findById(currentUser.getId()).orElseThrow();

            // Verify current password
            if (!PasswordUtil.verifyPassword(request.getCurrentPassword(), user.getPassword())) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Current password is incorrect"
                ));
            }

            // Validate new password
            if (request.getNewPassword().length() < 8) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "New password must be at least 8 characters"
                ));
            }
            if (!PASSWORD_PATTERN.matcher(request.getNewPassword()).matches()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "New password must contain at least 1 letter, 1 number, and 1 special character (@$!%*?&)"
                ));
            }

            // Update password
            user.setPassword(request.getNewPassword()); // Will be hashed by @PreUpdate
            userRepository.save(user);

            // Revoke all refresh tokens for security
            String ipAddress = getClientIp(httpRequest);
            refreshTokenService.revokeAllUserTokens(user.getId(), ipAddress);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Password changed successfully. Please login again."
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error changing password",
                    "error", e.getMessage()
            ));
        }
    }

    private String extractPublicId(String url) {
        if (url == null || !url.contains("/")) return null;
        try {
            String[] parts = url.split("/");
            String lastPart = parts[parts.length - 1];
            return lastPart.substring(0, lastPart.lastIndexOf('.'));
        } catch (Exception e) {
            return null;
        }
    }
}
