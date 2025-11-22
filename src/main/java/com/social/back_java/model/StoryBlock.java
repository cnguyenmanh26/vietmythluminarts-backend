package com.social.back_java.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

import jakarta.persistence.Column;
@Data
@Embeddable
public class StoryBlock {
    private String type; // "text" or "image"
    private int sortOrder; // Renamed from 'order' to avoid SQL keyword conflict

    @Column(length = 5000)
    private String content;

    // Image fields flattened
    private String imageUrl;
    private String imagePublicId;
    private String imageCaption;
    private String imageAlt;
}
