package com.social.vietmythluminartsbackend.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded document for blog images
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogImage {

    private String url;
    private String publicId;
    private String alt;
    @Builder.Default
    private Boolean isPrimary = false;
}

