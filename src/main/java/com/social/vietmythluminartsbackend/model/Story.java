package com.social.vietmythluminartsbackend.model;

import com.social.vietmythluminartsbackend.model.embedded.StoryBlock;
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
 * Story entity (similar to Blog but with story blocks)
 * Migrated from Node.js Story model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "stories")
public class Story {

    @Id
    private String id;

    @TextIndexed(weight = 10)
    private String title;

    @Indexed(unique = true)
    private String slug;

    @TextIndexed(weight = 5)
    private String description;

    @Builder.Default
    private List<StoryBlock> blocks = new ArrayList<>(); // Max 50

    @DBRef
    private User author;

    @Builder.Default
    private List<String> tags = new ArrayList<>(); // Max 10

    @Builder.Default
    private BlogStatus status = BlogStatus.draft; // Reuse BlogStatus enum

    private String featuredImage;

    @Builder.Default
    private Integer views = 0;

    private LocalDateTime publishedAt;

    @Builder.Default
    private Integer sortOrder = 0;

    private String youtubeUrl;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

