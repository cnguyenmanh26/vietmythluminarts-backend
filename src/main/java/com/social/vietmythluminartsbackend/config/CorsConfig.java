package com.social.vietmythluminartsbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CORS Configuration
 * Migrated from Node.js CORS setup
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors-origin:http://localhost:5173}") String corsOrigin
    ) {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins (configure via environment variable CORS_ORIGIN)
        // Supports multiple origins separated by comma
        List<String> allowedOrigins = new ArrayList<>();
        if (corsOrigin.contains(",")) {
            allowedOrigins.addAll(List.of(corsOrigin.split(",")));
        } else {
            allowedOrigins.add(corsOrigin);
        }
        // Always allow localhost for development
        allowedOrigins.add("http://localhost:5173");
        allowedOrigins.add("http://localhost:3000");
        allowedOrigins.add("http://localhost:5174");
        
        configuration.setAllowedOrigins(allowedOrigins);
        
        // Allow all HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
        // Allow all headers
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}

