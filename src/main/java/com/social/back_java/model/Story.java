package com.social.back_java.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "stories")
public class Story {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(unique = true)
    private String slug;

    @Column(length = 500)
    private String description;

    @ElementCollection
    @CollectionTable(name = "story_blocks", joinColumns = @JoinColumn(name = "story_id"))
    @OrderBy("sortOrder ASC")
    private List<StoryBlock> blocks = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @ElementCollection
    @CollectionTable(name = "story_tags", joinColumns = @JoinColumn(name = "story_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Column(columnDefinition = "VARCHAR(255) DEFAULT 'draft'")
    private String status = "draft";

    private String featuredImage;

    @Column(columnDefinition = "INT DEFAULT 0")
    private int views;

    @Temporal(TemporalType.TIMESTAMP)
    private Date publishedAt;

    @Column(columnDefinition = "INT DEFAULT 0")
    private int sortOrder;

    private String youtubeUrl;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}
