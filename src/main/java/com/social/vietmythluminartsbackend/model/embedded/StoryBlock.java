package com.social.vietmythluminartsbackend.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded document for story blocks (text or image)
 * Allows rich product storytelling with interleaved text and images
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryBlock {

    /**
     * Block type: "text" or "image"
     */
    private String type;

    /**
     * Display order (0, 1, 2, ...)
     */
    private Integer order;

    /**
     * Text content (for text blocks)
     * Max 5000 characters
     */
    private String content;

    /**
     * Image data (for image blocks)
     */
    private StoryImage image;

    /**
     * Nested class for story image data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoryImage {
        /**
         * Cloudinary URL
         */
        private String url;

        /**
         * Cloudinary public ID
         */
        private String publicId;

        /**
         * Image caption
         */
        private String caption;

        /**
         * Alt text for SEO
         */
        private String alt;
    }
}

