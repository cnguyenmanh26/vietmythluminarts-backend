package com.social.vietmythluminartsbackend.exception;

/**
 * Exception thrown for authentication and authorization errors
 */
public class AuthException extends RuntimeException {

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}

