package com.social.back_java.model;

import com.social.back_java.util.PasswordUtil;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Column(columnDefinition = "VARCHAR(255) DEFAULT 'local'")
    private String provider = "local";

    @Column(unique = true)
    private String googleId;

    private String avatar;

    private String phoneNumber;

    private String gender;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(255) DEFAULT 'USER'")
    private Role role = Role.user;

    @Column(columnDefinition = "BOOLEAN DEFAULT true")
    private boolean isActive = true;

    private Date lastLogin;

    private String resetPasswordToken;

    private Date resetPasswordExpire;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
        hashPasswordIfNeeded();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
        hashPasswordIfNeeded();
    }

    private void hashPasswordIfNeeded() {
        if (password != null && !password.startsWith("$2a$")) {
            password = PasswordUtil.hashPassword(password);
        }
    }
}
