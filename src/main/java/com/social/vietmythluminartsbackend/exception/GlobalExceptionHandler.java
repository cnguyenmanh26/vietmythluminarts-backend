package com.social.vietmythluminartsbackend.exception;

import com.social.vietmythluminartsbackend.dto.response.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for all controllers
 * Provides standardized error responses
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle validation errors (Bean Validation)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Void> response = ApiResponse.error("Validation failed", errors);
        
        log.warn("Validation error: {}", errors);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle authentication exceptions
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthException(
            AuthException ex, WebRequest request) {
        
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        
        log.warn("Auth exception: {} - Path: {}", 
                ex.getMessage(), request.getDescription(false));
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handle Spring Security authentication exceptions
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            Exception ex, WebRequest request) {
        
        String message = "Invalid email or password";
        ApiResponse<Void> response = ApiResponse.error(message);
        
        log.warn("Authentication failed: {} - Path: {}", 
                ex.getMessage(), request.getDescription(false));
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handle token exceptions
     */
    @ExceptionHandler(TokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenException(
            TokenException ex, WebRequest request) {
        
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        
        log.warn("Token exception: {} - Path: {}", 
                ex.getMessage(), request.getDescription(false));
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handle JWT exceptions
     */
    @ExceptionHandler({JwtException.class, ExpiredJwtException.class})
    public ResponseEntity<ApiResponse<Void>> handleJwtException(
            Exception ex, WebRequest request) {
        
        String message = ex instanceof ExpiredJwtException 
                ? "JWT token has expired" 
                : "Invalid JWT token";
        
        ApiResponse<Void> response = ApiResponse.error(message);
        
        log.warn("JWT exception: {} - Path: {}", 
                ex.getMessage(), request.getDescription(false));
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handle resource not found exceptions
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        
        log.warn("Resource not found: {} - Path: {}", 
                ex.getMessage(), request.getDescription(false));
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        
        log.warn("Illegal argument: {} - Path: {}", 
                ex.getMessage(), request.getDescription(false));
        
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle unsupported operation exceptions
     */
    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupportedOperationException(
            UnsupportedOperationException ex, WebRequest request) {
        
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        
        log.warn("Unsupported operation: {} - Path: {}", 
                ex.getMessage(), request.getDescription(false));
        
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(response);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(
            Exception ex, WebRequest request) {
        
        String message = "An unexpected error occurred. Please try again later.";
        ApiResponse<Void> response = ApiResponse.error(message);
        
        log.error("Unexpected error: {} - Path: {}", 
                ex.getMessage(), request.getDescription(false), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

