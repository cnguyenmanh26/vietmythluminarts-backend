package com.social.back_java.dto.auth;

import com.social.back_java.model.Address;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;
    private String email;
    private String phoneNumber;
    private String gender;
    private Address address;
    private Boolean removeAvatar;
}
