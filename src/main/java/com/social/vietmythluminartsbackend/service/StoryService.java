package com.social.vietmythluminartsbackend.service;

import com.social.vietmythluminartsbackend.exception.AuthException;
import com.social.vietmythluminartsbackend.exception.ResourceNotFoundException;
import com.social.vietmythluminartsbackend.model.Story;
import com.social.vietmythluminartsbackend.model.User;
import com.social.vietmythluminartsbackend.model.embedded.StoryBlock;
import com.social.vietmythluminartsbackend.model.enums.BlogStatus;
import com.social.vietmythluminartsbackend.model.enums.Role;
import com.social.vietmythluminartsbackend.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Service for story operations
 * Migrated from Node.js storyController.js
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StoryService {

    private final StoryRepository storyRepository;
    private final CloudinaryService cloudinaryService;

    private static final Pattern OBJECT_ID_PATTERN = Pattern.compile("^[0-9a-fA-F]{24}$");

    /**
     * Get all stories with pagination and filtering
     */
    public Page<Story> getStories(Pageable pageable, BlogStatus status, String search, 
                                  List<String> tags, User currentUser) {
        // Only admins can see drafts
        BlogStatus effectiveStatus = status;
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            effectiveStatus = BlogStatus.published;
        }

        if (search != null && !search.isEmpty()) {
            return storyRepository.searchStories(search, pageable);
        }

        if (effectiveStatus != null) {
            return storyRepository.findByStatus(effectiveStatus, pageable);
        } else if (tags != null && !tags.isEmpty()) {
            return storyRepository.findByTagsIn(tags, pageable);
        }

        return storyRepository.findAll(pageable);
    }

    /**
     * Get story by ID or slug
     */
    public Story getStoryByIdentifier(String identifier, User currentUser) {
        Story story = null;

        // Check if identifier is a valid MongoDB ObjectId
        if (OBJECT_ID_PATTERN.matcher(identifier).matches()) {
            story = storyRepository.findById(identifier).orElse(null);
        }

        // If not found by ID, try slug
        if (story == null) {
            story = storyRepository.findBySlug(identifier);
        }

        if (story == null) {
            throw new ResourceNotFoundException("Story not found");
        }

        // Check if user can view draft stories
        if (story.getStatus() == BlogStatus.draft) {
            if (currentUser == null ||
                (currentUser.getRole() != Role.ADMIN &&
                 !story.getAuthor().getId().equals(currentUser.getId()))) {
                throw new ResourceNotFoundException("Story not found");
            }
        }

        return story;
    }

    /**
     * Create new story
     */
    @Transactional
    public Story createStory(Story story, User author, MultipartFile featuredImageFile,
                            Map<String, MultipartFile> blockImageFiles) throws IOException {
        story.setAuthor(author);
        story.setStatus(story.getStatus() != null ? story.getStatus() : BlogStatus.draft);

        // Generate slug
        if (story.getSlug() == null && story.getTitle() != null) {
            story.setSlug(generateSlug(story.getTitle()));
        }

        // Set publishedAt when status is published
        if (story.getStatus() == BlogStatus.published && story.getPublishedAt() == null) {
            story.setPublishedAt(LocalDateTime.now());
        }

        // Upload featured image
        if (featuredImageFile != null && !featuredImageFile.isEmpty()) {
            Map<String, Object> uploadResult = cloudinaryService.uploadImage(featuredImageFile, "stories/featured");
            story.setFeaturedImage((String) uploadResult.get("url"));
        }

        // Process story blocks and upload images
        if (story.getBlocks() != null) {
            List<StoryBlock> processedBlocks = new ArrayList<>();
            for (int i = 0; i < story.getBlocks().size(); i++) {
                StoryBlock block = story.getBlocks().get(i);
                
                if ("image".equals(block.getType())) {
                    // Check if there's an uploaded file for this block
                    String fieldName = "blockImage_" + i;
                    MultipartFile imageFile = blockImageFiles != null ? blockImageFiles.get(fieldName) : null;
                    
                    if (imageFile != null && !imageFile.isEmpty()) {
                        // Upload image
                        Map<String, Object> uploadResult = cloudinaryService.uploadImage(imageFile, "stories/blocks");
                        
                        // Update block image
                        StoryBlock.StoryImage storyImage = StoryBlock.StoryImage.builder()
                                .url((String) uploadResult.get("url"))
                                .publicId((String) uploadResult.get("publicId"))
                                .caption(block.getImage() != null ? block.getImage().getCaption() : "")
                                .alt(block.getImage() != null ? block.getImage().getAlt() : "")
                                .build();
                        
                        block.setImage(storyImage);
                    }
                }
                
                processedBlocks.add(block);
            }
            story.setBlocks(processedBlocks);
        }

        Story savedStory = storyRepository.save(story);
        log.info("Story created: {}", savedStory.getId());

        return savedStory;
    }

    /**
     * Update story
     */
    @Transactional
    public Story updateStory(String id, Story storyUpdate, User currentUser,
                             MultipartFile featuredImageFile, Map<String, MultipartFile> blockImageFiles) throws IOException {
        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Story not found"));

        // Update fields
        if (storyUpdate.getTitle() != null) {
            story.setTitle(storyUpdate.getTitle());
            // Regenerate slug if title changed
            story.setSlug(generateSlug(storyUpdate.getTitle()));
        }
        if (storyUpdate.getDescription() != null) {
            story.setDescription(storyUpdate.getDescription());
        }
        if (storyUpdate.getStatus() != null) {
            story.setStatus(storyUpdate.getStatus());
            // Set publishedAt when status changes to published
            if (storyUpdate.getStatus() == BlogStatus.published && story.getPublishedAt() == null) {
                story.setPublishedAt(LocalDateTime.now());
            }
        }
        if (storyUpdate.getTags() != null) {
            story.setTags(storyUpdate.getTags());
        }
        if (storyUpdate.getSortOrder() != null) {
            story.setSortOrder(storyUpdate.getSortOrder());
        }
        if (storyUpdate.getYoutubeUrl() != null) {
            story.setYoutubeUrl(storyUpdate.getYoutubeUrl());
        }

        // Handle featured image
        if (featuredImageFile != null && !featuredImageFile.isEmpty()) {
            // Delete old featured image if exists
            if (story.getFeaturedImage() != null) {
                String publicId = cloudinaryService.extractPublicId(story.getFeaturedImage());
                if (publicId != null) {
                    cloudinaryService.deleteMedia(publicId, "image");
                }
            }
            // Upload new featured image
            Map<String, Object> uploadResult = cloudinaryService.uploadImage(featuredImageFile, "stories/featured");
            story.setFeaturedImage((String) uploadResult.get("url"));
        }

        // Handle blocks update
        if (storyUpdate.getBlocks() != null) {
            List<StoryBlock> processedBlocks = new ArrayList<>();
            for (int i = 0; i < storyUpdate.getBlocks().size(); i++) {
                StoryBlock block = storyUpdate.getBlocks().get(i);
                
                if ("image".equals(block.getType())) {
                    String fieldName = "blockImage_" + i;
                    MultipartFile imageFile = blockImageFiles != null ? blockImageFiles.get(fieldName) : null;
                    
                    if (imageFile != null && !imageFile.isEmpty()) {
                        // Upload new image
                        Map<String, Object> uploadResult = cloudinaryService.uploadImage(imageFile, "stories/blocks");
                        
                        StoryBlock.StoryImage storyImage = StoryBlock.StoryImage.builder()
                                .url((String) uploadResult.get("url"))
                                .publicId((String) uploadResult.get("publicId"))
                                .caption(block.getImage() != null ? block.getImage().getCaption() : "")
                                .alt(block.getImage() != null ? block.getImage().getAlt() : "")
                                .build();
                        
                        block.setImage(storyImage);
                    }
                }
                
                processedBlocks.add(block);
            }
            story.setBlocks(processedBlocks);
        }

        return storyRepository.save(story);
    }

    /**
     * Delete story
     */
    @Transactional
    public void deleteStory(String id) {
        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Story not found"));

        // Delete block images from Cloudinary
        if (story.getBlocks() != null) {
            story.getBlocks().stream()
                    .filter(block -> "image".equals(block.getType()))
                    .filter(block -> block.getImage() != null && block.getImage().getPublicId() != null)
                    .forEach(block -> cloudinaryService.deleteMedia(
                            block.getImage().getPublicId(), "image"));
        }

        // Delete featured image if exists
        if (story.getFeaturedImage() != null) {
            String publicId = cloudinaryService.extractPublicId(story.getFeaturedImage());
            if (publicId != null) {
                cloudinaryService.deleteMedia(publicId, "image");
            }
        }

        storyRepository.deleteById(id);
        log.info("Story deleted: {}", id);
    }

    /**
     * Get popular tags
     */
    public List<Map<String, Object>> getPopularTags(int limit) {
        // Simplified - would need aggregation in real implementation
        List<Story> publishedStories = storyRepository.findByStatus(BlogStatus.published, Pageable.unpaged()).getContent();
        // Count tag occurrences
        return publishedStories.stream()
                .flatMap(story -> story.getTags().stream())
                .distinct()
                .limit(limit)
                .map(tag -> Map.<String, Object>of("tag", tag, "count",
                        publishedStories.stream()
                                .filter(s -> s.getTags().contains(tag))
                                .count()))
                .toList();
    }

    // ==================== Helper Methods ====================

    /**
     * Generate slug from title
     */
    private String generateSlug(String title) {
        String slug = title.toLowerCase()
                .replaceAll("[^\\w\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
        
        return slug + "-" + System.currentTimeMillis();
    }
}

