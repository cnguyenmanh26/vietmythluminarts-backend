package com.social.back_java.dto.auth;

import com.social.back_java.model.Address;
import com.social.back_java.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String gender;
    private Address address;
    private Role role;
    private String avatar;
    private String provider;
    private boolean isActive;
    private Date lastLogin;
    private Date createdAt;
    private Date updatedAt;
}
