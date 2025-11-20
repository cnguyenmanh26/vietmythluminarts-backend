package com.social.vietmythluminartsbackend.exception;

/**
 * Exception thrown for token-related errors (JWT, Refresh Token)
 */
public class TokenException extends RuntimeException {

    public TokenException(String message) {
        super(message);
    }

    public TokenException(String message, Throwable cause) {
        super(message, cause);
    }
}

