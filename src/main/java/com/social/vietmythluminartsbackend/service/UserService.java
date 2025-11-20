package com.social.vietmythluminartsbackend.service;

import com.social.vietmythluminartsbackend.dto.request.UpdateProfileRequest;
import com.social.vietmythluminartsbackend.dto.response.UserResponse;
import com.social.vietmythluminartsbackend.exception.AuthException;
import com.social.vietmythluminartsbackend.exception.ResourceNotFoundException;
import com.social.vietmythluminartsbackend.model.User;
import com.social.vietmythluminartsbackend.repository.UserRepository;
import com.social.vietmythluminartsbackend.util.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Service for user management operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CloudinaryService cloudinaryService;

    /**
     * Get user by ID
     * @param userId User ID
     * @return UserResponse
     */
    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return userMapper.toResponse(user);
    }

    /**
     * Get current user profile
     * @param userId Current user's ID
     * @return UserResponse
     */
    public UserResponse getCurrentUser(String userId) {
        return getUserById(userId);
    }

    /**
     * Update user profile
     * @param userId User ID
     * @param request Update profile request
     * @return Updated UserResponse
     */
    @Transactional
    public UserResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Update name
        if (request.getName() != null) {
            user.setName(request.getName());
        }

        // Update email (check uniqueness)
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new AuthException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }

        // Update phone number (check uniqueness)
        if (request.getPhoneNumber() != null && 
            !request.getPhoneNumber().equals(user.getPhoneNumber())) {
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new AuthException("Phone number already in use");
            }
            user.setPhoneNumber(request.getPhoneNumber());
        }

        // Update gender
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }

        // Update address
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }

        // Remove avatar if requested
        if (Boolean.TRUE.equals(request.getRemoveAvatar())) {
            // TODO: Delete old avatar from Cloudinary if exists
            user.setAvatar(null);
        }

        User updatedUser = userRepository.save(user);
        log.info("Profile updated for user: {}", updatedUser.getEmail());

        return userMapper.toResponse(updatedUser);
    }

    /**
     * Update user avatar
     * @param userId User ID
     * @param file Avatar file
     * @return Updated UserResponse
     */
    @Transactional
    public UserResponse updateAvatar(String userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        try {
            String oldAvatar = user.getAvatar();
            
            // Upload new avatar to Cloudinary
            Map<String, Object> uploadResult = cloudinaryService.uploadImage(file, "avatars");
            String newAvatarUrl = (String) uploadResult.get("url");
            
            user.setAvatar(newAvatarUrl);
            User updatedUser = userRepository.save(user);
            
            // Delete old avatar if exists and not from Google
            if (oldAvatar != null && !oldAvatar.contains("googleusercontent.com")) {
                String publicId = cloudinaryService.extractPublicId(oldAvatar);
                if (publicId != null) {
                    cloudinaryService.deleteMedia(publicId, "image");
                }
            }

            log.info("Avatar updated for user: {}", user.getEmail());
            return userMapper.toResponse(updatedUser);
        } catch (IOException e) {
            log.error("Failed to upload avatar for user: {}", userId, e);
            throw new RuntimeException("Failed to upload avatar: " + e.getMessage());
        }
    }

    /**
     * Deactivate user account
     * @param userId User ID
     */
    @Transactional
    public void deactivateAccount(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setIsActive(false);
        userRepository.save(user);

        log.info("Account deactivated: {}", user.getEmail());
    }

    /**
     * Reactivate user account (admin only)
     * @param userId User ID
     */
    @Transactional
    public void reactivateAccount(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setIsActive(true);
        userRepository.save(user);

        log.info("Account reactivated: {}", user.getEmail());
    }

    /**
     * Check if user exists by email
     * @param email Email address
     * @return true if exists
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Get user entity by ID (internal use)
     * @param userId User ID
     * @return User entity
     */
    public User findById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Get user entity by email (internal use)
     * @param email Email address
     * @return User entity
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}

