package com.social.vietmythluminartsbackend.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cloudinary configuration for image and video upload
 * Migrated from Node.js config/cloudinary.js
 */
@Slf4j
@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Value("${cloudinary.secure:true}")
    private Boolean secure;

    /**
     * Create Cloudinary bean
     * @return Configured Cloudinary instance
     */
    @Bean
    public Cloudinary cloudinary() {
        try {
            Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret,
                    "secure", secure
            ));

            // Validate configuration
            validateCloudinaryConfig();
            
            log.info("✅ Cloudinary configured successfully for cloud: {}", cloudName);
            return cloudinary;
        } catch (Exception e) {
            log.error("❌ Failed to configure Cloudinary: {}", e.getMessage());
            throw new RuntimeException("Failed to configure Cloudinary", e);
        }
    }

    /**
     * Validate Cloudinary configuration
     */
    private void validateCloudinaryConfig() {
        if (cloudName == null || cloudName.isEmpty() || cloudName.equals("your_cloud_name")) {
            log.warn("⚠️  Cloudinary cloud name not configured properly");
        }
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your_api_key")) {
            log.warn("⚠️  Cloudinary API key not configured properly");
        }
        if (apiSecret == null || apiSecret.isEmpty() || apiSecret.equals("your_api_secret")) {
            log.warn("⚠️  Cloudinary API secret not configured properly");
        }
    }
}

