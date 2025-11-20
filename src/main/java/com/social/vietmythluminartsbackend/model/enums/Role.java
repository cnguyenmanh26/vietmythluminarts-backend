package com.social.vietmythluminartsbackend.model.enums;

/**
 * User roles for authorization
 */
public enum Role {
    /**
     * Regular user - Default role
     * Can view products, create orders, manage cart
     */
    USER,
    
    /**
     * Administrator
     * Full access to all resources and operations
     * Can manage users, products, orders, blogs, stories
     */
    ADMIN
}

