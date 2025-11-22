package com.social.back_java.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Data
@Entity
@Table(name = "blogs")
public class Blog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(unique = true)
    private String slug;

    @Column(length = 500)
    private String excerpt;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    private String featuredImage;

    @ElementCollection
    @CollectionTable(name = "blog_images", joinColumns = @JoinColumn(name = "blog_id"))
    private List<ProductImage> images = new ArrayList<>(); // Reusing ProductImage structure as it is identical

    @Column(nullable = false)
    private String category;

    @ElementCollection
    @CollectionTable(name = "blog_tags", joinColumns = @JoinColumn(name = "blog_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Column(columnDefinition = "VARCHAR(255) DEFAULT 'draft'")
    private String status = "draft";

    @Column(columnDefinition = "INT DEFAULT 0")
    private int views;

    @ManyToMany
    @JoinTable(
        name = "blog_likes",
        joinColumns = @JoinColumn(name = "blog_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likes = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "blog_id") // Unidirectional OneToMany for simplicity as Comment doesn't strictly need to know about Blog in this design, or we can add it.
    private List<Comment> comments = new ArrayList<>();

    private String metaTitle;
    private String metaDescription;

    @Temporal(TemporalType.TIMESTAMP)
    private Date publishedAt;

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
