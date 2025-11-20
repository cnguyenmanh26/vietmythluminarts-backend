package com.social.vietmythluminartsbackend.dto.response;

import com.social.vietmythluminartsbackend.model.embedded.Address;
import com.social.vietmythluminartsbackend.model.enums.Gender;
import com.social.vietmythluminartsbackend.model.enums.Provider;
import com.social.vietmythluminartsbackend.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for user information
 * Excludes sensitive fields like password
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;
    private String name;
    private String email;
    private String phoneNumber;
    private String avatar;
    private Gender gender;
    private Address address;
    private Provider provider;
    private Role role;
    private Boolean isActive;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

