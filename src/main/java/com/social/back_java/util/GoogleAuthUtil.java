package com.social.back_java.util;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class GoogleAuthUtil {

    @Value("${google.client.id}")
    private String googleClientId;

    /**
     * Verify Google ID token
     * @param idTokenString Google ID token from frontend
     * @return GoogleIdToken.Payload if valid
     * @throws Exception if token is invalid
     */
    public GoogleIdToken.Payload verifyGoogleToken(String idTokenString) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        
        if (idToken != null) {
            return idToken.getPayload();
        } else {
            throw new Exception("Invalid Google ID token");
        }
    }

    /**
     * Check if Google OAuth is configured
     * @return true if configured, false otherwise
     */
    public boolean isGoogleAuthConfigured() {
        return googleClientId != null && !googleClientId.isEmpty() 
                && !googleClientId.equals("your_google_client_id_here");
    }
}
