package com.social.vietmythluminartsbackend.dto.request;

import com.social.vietmythluminartsbackend.model.embedded.Address;
import com.social.vietmythluminartsbackend.model.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating user profile
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ\\s]+$", message = "Name can only contain letters and spaces")
    private String name;

    @Email(message = "Please provide a valid email address")
    private String email;

    @Pattern(
        regexp = "^(\\+84|0)[3|5|7|8|9][0-9]{8}$",
        message = "Please provide a valid Vietnamese phone number"
    )
    private String phoneNumber;

    private Gender gender;

    private Address address;

    private Boolean removeAvatar;
}

