package com.social.vietmythluminartsbackend.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Google OAuth service for verifying Google ID tokens
 * Migrated from Node.js utils/googleAuth.js
 */
@Slf4j
@Service
public class GoogleAuthService {

    @Value("${google.client-id:}")
    private String googleClientId;

    private GoogleIdTokenVerifier verifier;

    /**
     * Get or create Google ID token verifier
     */
    private GoogleIdTokenVerifier getVerifier() {
        if (verifier == null && googleClientId != null && !googleClientId.isEmpty()) {
            verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    new GsonFactory()
            )
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
        }
        return verifier;
    }

    /**
     * Verify Google ID token
     * @param idToken Google ID token from frontend
     * @return GoogleUserInfo if valid, null otherwise
     */
    public GoogleUserInfo verifyToken(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = getVerifier();
            
            if (verifier == null) {
                log.warn("Google OAuth not configured - GOOGLE_CLIENT_ID missing");
                return null;
            }

            GoogleIdToken token = verifier.verify(idToken);
            
            if (token == null) {
                log.warn("Invalid Google ID token");
                return null;
            }

            GoogleIdToken.Payload payload = token.getPayload();

            return GoogleUserInfo.builder()
                    .googleId(payload.getSubject())
                    .email((String) payload.get("email"))
                    .name((String) payload.get("name"))
                    .avatar((String) payload.get("picture"))
                    .emailVerified((Boolean) payload.get("email_verified"))
                    .build();
        } catch (Exception e) {
            log.error("Failed to verify Google token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Check if Google OAuth is configured
     */
    public boolean isConfigured() {
        return googleClientId != null && !googleClientId.isEmpty();
    }

    /**
     * Google user info DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class GoogleUserInfo {
        private String googleId;
        private String email;
        private String name;
        private String avatar;
        private Boolean emailVerified;
    }
}

