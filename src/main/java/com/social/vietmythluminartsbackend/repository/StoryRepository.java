package com.social.vietmythluminartsbackend.repository;

import com.social.vietmythluminartsbackend.model.Story;
import com.social.vietmythluminartsbackend.model.enums.BlogStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Story entity
 */
@Repository
public interface StoryRepository extends MongoRepository<Story, String> {

    /**
     * Find story by slug
     */
    Story findBySlug(String slug);

    /**
     * Find stories by status
     */
    Page<Story> findByStatus(BlogStatus status, Pageable pageable);

    /**
     * Find stories by author
     */
    Page<Story> findByAuthorId(String authorId, Pageable pageable);

    /**
     * Search stories by title, description, or tags (text search)
     */
    @Query("{ $text: { $search: ?0 } }")
    Page<Story> searchStories(String search, Pageable pageable);

    /**
     * Find stories with tags containing any of the provided tags
     */
    Page<Story> findByTagsIn(List<String> tags, Pageable pageable);

    /**
     * Find stories ordered by sortOrder
     */
    Page<Story> findByStatusOrderBySortOrderAscPublishedAtDesc(BlogStatus status, Pageable pageable);
}

