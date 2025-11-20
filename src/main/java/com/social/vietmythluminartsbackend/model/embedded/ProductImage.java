package com.social.vietmythluminartsbackend.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded document for product images
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {

    /**
     * Cloudinary URL
     */
    private String url;

    /**
     * Cloudinary public ID for deletion
     */
    private String publicId;

    /**
     * Alt text for SEO
     */
    private String alt;

    /**
     * Is this the primary/featured image
     */
    @Builder.Default
    private Boolean isPrimary = false;
}

