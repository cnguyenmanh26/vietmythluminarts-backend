package com.social.vietmythluminartsbackend.util;

import com.social.vietmythluminartsbackend.dto.response.UserResponse;
import com.social.vietmythluminartsbackend.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapper utility for converting User entity to UserResponse DTO
 */
@Component
public class UserMapper {

    /**
     * Convert User entity to UserResponse DTO
     * Excludes sensitive information like password
     * @param user User entity
     * @return UserResponse DTO
     */
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .address(user.getAddress())
                .provider(user.getProvider())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

