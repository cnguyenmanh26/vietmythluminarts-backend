package com.social.vietmythluminartsbackend.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded document for product videos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVideo {

    /**
     * Cloudinary video URL
     */
    private String url;

    /**
     * Cloudinary public ID for deletion
     */
    private String publicId;

    /**
     * Auto-generated thumbnail URL
     */
    private String thumbnail;

    /**
     * Video duration in seconds
     */
    private Double duration;
}

