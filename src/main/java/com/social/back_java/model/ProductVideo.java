package com.social.back_java.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class ProductVideo {
    private String url;
    private String publicId;
    private String thumbnail;
    private Double duration;
}
