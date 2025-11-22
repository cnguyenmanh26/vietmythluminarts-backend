package com.social.back_java.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class ProductImage {
    private String url;
    private String publicId;
    private String alt;
    private boolean isPrimary;
}
