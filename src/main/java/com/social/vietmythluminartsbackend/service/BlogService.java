package com.social.vietmythluminartsbackend.service;

import com.social.vietmythluminartsbackend.exception.AuthException;
import com.social.vietmythluminartsbackend.exception.ResourceNotFoundException;
import com.social.vietmythluminartsbackend.model.Blog;
import com.social.vietmythluminartsbackend.model.User;
import com.social.vietmythluminartsbackend.model.embedded.BlogComment;
import com.social.vietmythluminartsbackend.model.embedded.BlogImage;
import com.social.vietmythluminartsbackend.model.enums.BlogCategory;
import com.social.vietmythluminartsbackend.model.enums.BlogStatus;
import com.social.vietmythluminartsbackend.model.enums.Role;
import com.social.vietmythluminartsbackend.repository.BlogRepository;
import com.social.vietmythluminartsbackend.repository.UserRepository;
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
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Service for blog operations
 * Migrated from Node.js blogController.js
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    private static final Pattern OBJECT_ID_PATTERN = Pattern.compile("^[0-9a-fA-F]{24}$");

    /**
     * Get all blogs with pagination and filtering
     */
    public Page<Blog> getBlogs(Pageable pageable, BlogCategory category, BlogStatus status, 
                               String search, List<String> tags, User currentUser) {
        // Only admins can see drafts
        BlogStatus effectiveStatus = status;
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            effectiveStatus = BlogStatus.published;
        }

        if (search != null && !search.isEmpty()) {
            return blogRepository.searchBlogs(search, pageable);
        }

        if (category != null && effectiveStatus != null) {
            return blogRepository.findByCategoryAndStatus(category, effectiveStatus, pageable);
        } else if (category != null) {
            return blogRepository.findByCategory(category, pageable);
        } else if (effectiveStatus != null) {
            return blogRepository.findByStatus(effectiveStatus, pageable);
        } else if (tags != null && !tags.isEmpty()) {
            return blogRepository.findByTagsIn(tags, pageable);
        }

        return blogRepository.findAll(pageable);
    }

    /**
     * Get blog by ID or slug
     */
    public Blog getBlogByIdentifier(String identifier, User currentUser) {
        Blog blog = null;

        // Check if identifier is a valid MongoDB ObjectId
        if (OBJECT_ID_PATTERN.matcher(identifier).matches()) {
            blog = blogRepository.findById(identifier).orElse(null);
        }

        // If not found by ID, try slug
        if (blog == null) {
            blog = blogRepository.findBySlug(identifier);
        }

        if (blog == null) {
            throw new ResourceNotFoundException("Blog post not found");
        }

        // Check if user can view draft posts
        if (blog.getStatus() == BlogStatus.draft) {
            if (currentUser == null || 
                (currentUser.getRole() != Role.ADMIN && 
                 !blog.getAuthor().getId().equals(currentUser.getId()))) {
                throw new ResourceNotFoundException("Blog post not found");
            }
        }

        // Increment views
        blog.setViews(blog.getViews() + 1);
        blogRepository.save(blog);

        return blog;
    }

    /**
     * Create new blog
     */
    @Transactional
    public Blog createBlog(Blog blog, User author, MultipartFile featuredImageFile, 
                          List<MultipartFile> imageFiles) throws IOException {
        blog.setAuthor(author);
        blog.setStatus(blog.getStatus() != null ? blog.getStatus() : BlogStatus.draft);

        // Generate slug
        if (blog.getSlug() == null && blog.getTitle() != null) {
            blog.setSlug(generateSlug(blog.getTitle()));
        }

        // Auto-generate excerpt if not provided
        if (blog.getExcerpt() == null && blog.getContent() != null) {
            String excerpt = blog.getContent().substring(0, Math.min(200, blog.getContent().length())).trim();
            blog.setExcerpt(excerpt + "...");
        }

        // Set publishedAt when status is published
        if (blog.getStatus() == BlogStatus.published && blog.getPublishedAt() == null) {
            blog.setPublishedAt(LocalDateTime.now());
        }

        // Upload featured image
        if (featuredImageFile != null && !featuredImageFile.isEmpty()) {
            Map<String, Object> uploadResult = cloudinaryService.uploadImage(featuredImageFile, "blogs");
            blog.setFeaturedImage((String) uploadResult.get("url"));
        }

        // Upload additional images
        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<Map<String, Object>> uploadResults = cloudinaryService.uploadMultipleImages(imageFiles, "blogs");
            List<BlogImage> blogImages = new ArrayList<>();
            
            for (int i = 0; i < uploadResults.size(); i++) {
                Map<String, Object> result = uploadResults.get(i);
                BlogImage image = BlogImage.builder()
                        .url((String) result.get("url"))
                        .publicId((String) result.get("publicId"))
                        .alt(blog.getTitle() + " - Image " + (i + 1))
                        .isPrimary(i == 0)
                        .build();
                blogImages.add(image);
            }
            blog.setImages(blogImages);
        }

        Blog savedBlog = blogRepository.save(blog);
        log.info("Blog created: {}", savedBlog.getId());

        return savedBlog;
    }

    /**
     * Update blog
     */
    @Transactional
    public Blog updateBlog(String id, Blog blogUpdate, User currentUser, 
                          MultipartFile featuredImageFile, List<MultipartFile> imageFiles,
                          List<String> deletedImagePublicIds) throws IOException {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));

        // Check ownership (admin or author can update)
        if (currentUser.getRole() != Role.ADMIN && 
            !blog.getAuthor().getId().equals(currentUser.getId())) {
            throw new AuthException("You don't have permission to update this blog");
        }

        // Update fields
        if (blogUpdate.getTitle() != null) {
            blog.setTitle(blogUpdate.getTitle());
            // Regenerate slug if title changed
            blog.setSlug(generateSlug(blogUpdate.getTitle()));
        }
        if (blogUpdate.getContent() != null) {
            blog.setContent(blogUpdate.getContent());
        }
        if (blogUpdate.getExcerpt() != null) {
            blog.setExcerpt(blogUpdate.getExcerpt());
        }
        if (blogUpdate.getCategory() != null) {
            blog.setCategory(blogUpdate.getCategory());
        }
        if (blogUpdate.getTags() != null) {
            blog.setTags(blogUpdate.getTags());
        }
        if (blogUpdate.getStatus() != null) {
            blog.setStatus(blogUpdate.getStatus());
            // Set publishedAt when status changes to published
            if (blogUpdate.getStatus() == BlogStatus.published && blog.getPublishedAt() == null) {
                blog.setPublishedAt(LocalDateTime.now());
            }
        }
        if (blogUpdate.getMetaTitle() != null) {
            blog.setMetaTitle(blogUpdate.getMetaTitle());
        }
        if (blogUpdate.getMetaDescription() != null) {
            blog.setMetaDescription(blogUpdate.getMetaDescription());
        }

        // Handle featured image
        if (featuredImageFile != null && !featuredImageFile.isEmpty()) {
            // Delete old featured image
            if (blog.getFeaturedImage() != null) {
                String publicId = cloudinaryService.extractPublicId(blog.getFeaturedImage());
                if (publicId != null) {
                    cloudinaryService.deleteMedia(publicId, "image");
                }
            }
            // Upload new featured image
            Map<String, Object> uploadResult = cloudinaryService.uploadImage(featuredImageFile, "blogs");
            blog.setFeaturedImage((String) uploadResult.get("url"));
        }

        // Handle image deletion
        if (deletedImagePublicIds != null && !deletedImagePublicIds.isEmpty()) {
            for (String publicId : deletedImagePublicIds) {
                cloudinaryService.deleteMedia(publicId, "image");
            }
            blog.getImages().removeIf(img -> deletedImagePublicIds.contains(img.getPublicId()));
        }

        // Handle new image uploads
        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<Map<String, Object>> uploadResults = cloudinaryService.uploadMultipleImages(imageFiles, "blogs");
            for (Map<String, Object> result : uploadResults) {
                BlogImage image = BlogImage.builder()
                        .url((String) result.get("url"))
                        .publicId((String) result.get("publicId"))
                        .alt(blog.getTitle() + " - Image " + (blog.getImages().size() + 1))
                        .isPrimary(false)
                        .build();
                blog.getImages().add(image);
            }
        }

        return blogRepository.save(blog);
    }

    /**
     * Delete blog
     */
    @Transactional
    public void deleteBlog(String id, User currentUser) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));

        // Check ownership (admin or author can delete)
        if (currentUser.getRole() != Role.ADMIN && 
            !blog.getAuthor().getId().equals(currentUser.getId())) {
            throw new AuthException("You don't have permission to delete this blog");
        }

        // Delete featured image from Cloudinary
        if (blog.getFeaturedImage() != null) {
            String publicId = cloudinaryService.extractPublicId(blog.getFeaturedImage());
            if (publicId != null) {
                cloudinaryService.deleteMedia(publicId, "image");
            }
        }

        // Delete all images from Cloudinary
        if (blog.getImages() != null && !blog.getImages().isEmpty()) {
            List<String> publicIds = blog.getImages().stream()
                    .map(BlogImage::getPublicId)
                    .toList();
            cloudinaryService.deleteMultipleImages(publicIds);
        }

        blogRepository.deleteById(id);
        log.info("Blog deleted: {}", id);
    }

    /**
     * Like/Unlike blog
     */
    @Transactional
    public Blog toggleLike(String id, User user) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));

        // Check if user already liked
        boolean alreadyLiked = blog.getLikes().stream()
                .anyMatch(likedUser -> likedUser.getId().equals(user.getId()));

        if (alreadyLiked) {
            // Unlike
            blog.getLikes().removeIf(likedUser -> likedUser.getId().equals(user.getId()));
        } else {
            // Like
            blog.getLikes().add(user);
        }

        return blogRepository.save(blog);
    }

    /**
     * Add comment to blog
     */
    @Transactional
    public Blog addComment(String id, String content, User user) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));

        // Only allow comments on published blogs
        if (blog.getStatus() != BlogStatus.published) {
            throw new AuthException("Cannot comment on unpublished blogs");
        }

        BlogComment comment = BlogComment.builder()
                .user(user)
                .content(content.trim())
                .createdAt(LocalDateTime.now())
                .build();

        blog.getComments().add(comment);
        return blogRepository.save(blog);
    }

    /**
     * Delete comment from blog
     * Note: In MongoDB embedded documents, we need to find by index or use a different approach
     */
    @Transactional
    public Blog deleteComment(String blogId, int commentIndex, User currentUser) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));

        if (commentIndex < 0 || commentIndex >= blog.getComments().size()) {
            throw new ResourceNotFoundException("Comment not found");
        }

        BlogComment comment = blog.getComments().get(commentIndex);

        // Check if user is comment owner or admin
        if (!comment.getUser().getId().equals(currentUser.getId()) && 
            currentUser.getRole() != Role.ADMIN) {
            throw new AuthException("You don't have permission to delete this comment");
        }

        blog.getComments().remove(commentIndex);
        return blogRepository.save(blog);
    }

    /**
     * Get categories with count
     */
    public List<Map<String, Object>> getCategories() {
        // This would require aggregation - simplified version
        List<Blog> publishedBlogs = blogRepository.findByStatus(BlogStatus.published, Pageable.unpaged()).getContent();
        // Group by category and count
        // For now, return distinct categories
        return publishedBlogs.stream()
                .map(Blog::getCategory)
                .distinct()
                .map(cat -> Map.<String, Object>of("_id", cat.name(), "count", 
                        publishedBlogs.stream().filter(b -> b.getCategory() == cat).count()))
                .toList();
    }

    /**
     * Get popular tags
     */
    public List<Map<String, Object>> getPopularTags(int limit) {
        // Simplified - would need aggregation in real implementation
        List<Blog> publishedBlogs = blogRepository.findByStatus(BlogStatus.published, Pageable.unpaged()).getContent();
        // Count tag occurrences
        // For now, return all unique tags
        return publishedBlogs.stream()
                .flatMap(blog -> blog.getTags().stream())
                .distinct()
                .limit(limit)
                .map(tag -> Map.<String, Object>of("_id", tag, "count", 
                        publishedBlogs.stream()
                                .filter(b -> b.getTags().contains(tag))
                                .count()))
                .toList();
    }

    // ==================== Helper Methods ====================

    /**
     * Generate slug from title
     */
    private String generateSlug(String title) {
        String slug = title.toLowerCase()
                .replaceAll("[^\\w\\s-]", "") // Remove special characters
                .replaceAll("\\s+", "-") // Replace spaces with -
                .replaceAll("-+", "-") // Replace multiple - with single -
                .trim();
        
        // Add timestamp to ensure uniqueness
        return slug + "-" + System.currentTimeMillis();
    }
}

