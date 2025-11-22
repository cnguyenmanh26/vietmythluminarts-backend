package com.social.back_java.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class ProductSnapshot {
    private String name;
    private String image;
    private String category;
}
