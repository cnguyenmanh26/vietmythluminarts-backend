package com.social.vietmythluminartsbackend.repository;

import com.social.vietmythluminartsbackend.model.Blog;
import com.social.vietmythluminartsbackend.model.enums.BlogCategory;
import com.social.vietmythluminartsbackend.model.enums.BlogStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Blog entity
 */
@Repository
public interface BlogRepository extends MongoRepository<Blog, String> {

    /**
     * Find blog by slug
     */
    Blog findBySlug(String slug);

    /**
     * Find blogs by category
     */
    Page<Blog> findByCategory(BlogCategory category, Pageable pageable);

    /**
     * Find blogs by status
     */
    Page<Blog> findByStatus(BlogStatus status, Pageable pageable);

    /**
     * Find blogs by category and status
     */
    Page<Blog> findByCategoryAndStatus(BlogCategory category, BlogStatus status, Pageable pageable);

    /**
     * Find blogs by author
     */
    Page<Blog> findByAuthorId(String authorId, Pageable pageable);

    /**
     * Search blogs by title, content, or tags (text search)
     */
    @Query("{ $text: { $search: ?0 } }")
    Page<Blog> searchBlogs(String search, Pageable pageable);

    /**
     * Find blogs with tags containing any of the provided tags
     */
    Page<Blog> findByTagsIn(List<String> tags, Pageable pageable);

    /**
     * Get categories with count (aggregation)
     */
    @Query(value = "{ status: 'published' }", fields = "{ category: 1 }")
    List<Blog> findPublishedBlogsCategories();
}

