package com.social.vietmythluminartsbackend.model;

import com.social.vietmythluminartsbackend.model.embedded.BlogComment;
import com.social.vietmythluminartsbackend.model.embedded.BlogImage;
import com.social.vietmythluminartsbackend.model.enums.BlogCategory;
import com.social.vietmythluminartsbackend.model.enums.BlogStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Blog entity
 * Migrated from Node.js Blog model (WoodToy backend)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "blogs")
public class Blog {

    @Id
    private String id;

    @TextIndexed(weight = 10)
    private String title;

    @Indexed(unique = true)
    private String slug;

    @TextIndexed(weight = 5)
    private String excerpt;

    @TextIndexed(weight = 8)
    private String content; // No max length - supports very long blog posts

    @DBRef
    private User author;

    private String featuredImage;

    @Builder.Default
    private List<BlogImage> images = new ArrayList<>();

    private BlogCategory category;

    @Builder.Default
    private List<String> tags = new ArrayList<>(); // Max 10

    @Builder.Default
    private BlogStatus status = BlogStatus.draft;

    @Builder.Default
    private Integer views = 0;

    @Builder.Default
    @DBRef
    private List<User> likes = new ArrayList<>(); // User references

    @Builder.Default
    private List<BlogComment> comments = new ArrayList<>();

    private String metaTitle; // Max 70
    private String metaDescription; // Max 160

    private LocalDateTime publishedAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ==================== Business Logic Methods ====================

    public String getPrimaryImage() {
        if (images != null && !images.isEmpty()) {
            return images.stream()
                    .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                    .findFirst()
                    .map(BlogImage::getUrl)
                    .orElseGet(() -> images.get(0).getUrl());
        }
        return featuredImage;
    }

    public Integer getLikesCount() {
        return likes != null ? likes.size() : 0;
    }

    public Integer getCommentsCount() {
        return comments != null ? comments.size() : 0;
    }

    public Integer getReadingTime() {
        if (content == null) return 0;
        int words = content.split("\\s+").length;
        return (int) Math.ceil(words / 200.0); // 200 words per minute
    }
}

