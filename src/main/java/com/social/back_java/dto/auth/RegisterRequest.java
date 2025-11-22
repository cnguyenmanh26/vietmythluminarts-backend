package com.social.back_java.dto.auth;

import com.social.back_java.model.Address;
import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private String gender;
    private Address address;
}
