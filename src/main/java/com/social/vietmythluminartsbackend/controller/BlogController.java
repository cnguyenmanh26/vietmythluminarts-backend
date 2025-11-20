package com.social.vietmythluminartsbackend.controller;

import com.social.vietmythluminartsbackend.dto.request.AddCommentRequest;
import com.social.vietmythluminartsbackend.dto.request.CreateBlogRequest;
import com.social.vietmythluminartsbackend.dto.request.UpdateBlogRequest;
import com.social.vietmythluminartsbackend.dto.response.ApiResponse;
import com.social.vietmythluminartsbackend.model.Blog;
import com.social.vietmythluminartsbackend.model.User;
import com.social.vietmythluminartsbackend.model.enums.BlogCategory;
import com.social.vietmythluminartsbackend.model.enums.BlogStatus;
import com.social.vietmythluminartsbackend.service.BlogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for blog endpoints
 * Migrated from Node.js blogController.js
 * 
 * Base URL: /api/blogs
 */
@Slf4j
@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;
    private final com.social.vietmythluminartsbackend.service.UserService userService;

    /**
     * Get all blogs with pagination and filtering
     * GET /api/blogs
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Blog>>> getBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) BlogCategory category,
            @RequestParam(required = false) BlogStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tags,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Pageable pageable = PageRequest.of(page, limit, Sort.by("publishedAt", "createdAt").descending());

        List<String> tagList = null;
        if (tags != null && !tags.isEmpty()) {
            tagList = List.of(tags.split(","));
        }

        User currentUser = null;
        if (userDetails != null) {
            try {
                currentUser = userService.findByEmail(userDetails.getUsername());
            } catch (Exception e) {
                // User not found or not authenticated - continue as public
            }
        }

        Page<Blog> blogs = blogService.getBlogs(pageable, category, status, search, tagList, currentUser);

        ApiResponse<Page<Blog>> response = ApiResponse.success(
                "Blogs retrieved successfully",
                blogs
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get blog by ID or slug
     * GET /api/blogs/:identifier
     */
    @GetMapping("/{identifier}")
    public ResponseEntity<ApiResponse<Blog>> getBlogByIdentifier(
            @PathVariable String identifier,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = null;
        if (userDetails != null) {
            try {
                currentUser = userService.findByEmail(userDetails.getUsername());
            } catch (Exception e) {
                // Continue as public
            }
        }

        Blog blog = blogService.getBlogByIdentifier(identifier, currentUser);

        ApiResponse<Blog> response = ApiResponse.success(
                "Blog retrieved successfully",
                blog
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Create new blog (Admin only)
     * POST /api/blogs
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Blog>> createBlog(
            @Valid @ModelAttribute CreateBlogRequest request,
            @RequestParam(required = false) MultipartFile featuredImage,
            @RequestParam(required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            User author = userService.findByEmail(userDetails.getUsername());

            Blog blog = Blog.builder()
                    .title(request.getTitle())
                    .content(request.getContent())
                    .excerpt(request.getExcerpt())
                    .category(request.getCategory())
                    .tags(request.getTags() != null ? request.getTags() : List.of())
                    .status(request.getStatus() != null ? request.getStatus() : BlogStatus.draft)
                    .metaTitle(request.getMetaTitle())
                    .metaDescription(request.getMetaDescription())
                    .build();

            Blog createdBlog = blogService.createBlog(blog, author, featuredImage, images);

            ApiResponse<Blog> response = ApiResponse.success(
                    "Blog created successfully",
                    createdBlog
            );

            return ResponseEntity.status(201).body(response);
        } catch (Exception e) {
            log.error("Failed to create blog: {}", e.getMessage());
            throw new RuntimeException("Failed to create blog: " + e.getMessage());
        }
    }

    /**
     * Update blog (Admin or Author)
     * PUT /api/blogs/:id
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Blog>> updateBlog(
            @PathVariable String id,
            @Valid @ModelAttribute UpdateBlogRequest request,
            @RequestParam(required = false) MultipartFile featuredImage,
            @RequestParam(required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            User currentUser = userService.findByEmail(userDetails.getUsername());

            Blog blogUpdate = Blog.builder()
                    .title(request.getTitle())
                    .content(request.getContent())
                    .excerpt(request.getExcerpt())
                    .category(request.getCategory())
                    .tags(request.getTags())
                    .status(request.getStatus())
                    .metaTitle(request.getMetaTitle())
                    .metaDescription(request.getMetaDescription())
                    .build();

            Blog updatedBlog = blogService.updateBlog(id, blogUpdate, currentUser, 
                    featuredImage, images, request.getDeletedImages());

            ApiResponse<Blog> response = ApiResponse.success(
                    "Blog updated successfully",
                    updatedBlog
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to update blog: {}", e.getMessage());
            throw new RuntimeException("Failed to update blog: " + e.getMessage());
        }
    }

    /**
     * Delete blog (Admin or Author)
     * DELETE /api/blogs/:id
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteBlog(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userService.findByEmail(userDetails.getUsername());
        blogService.deleteBlog(id, currentUser);

        ApiResponse<Void> response = ApiResponse.success(
                "Blog deleted successfully",
                null
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Like/Unlike blog
     * POST /api/blogs/:id/like
     */
    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Blog>> toggleLike(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        Blog blog = blogService.toggleLike(id, user);

        ApiResponse<Blog> response = ApiResponse.success(
                "Blog " + (blog.getLikes().contains(user) ? "liked" : "unliked") + " successfully",
                blog
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Add comment to blog
     * POST /api/blogs/:id/comments
     */
    @PostMapping("/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Blog>> addComment(
            @PathVariable String id,
            @Valid @RequestBody AddCommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        Blog blog = blogService.addComment(id, request.getContent(), user);

        ApiResponse<Blog> response = ApiResponse.success(
                "Comment added successfully",
                blog
        );

        return ResponseEntity.status(201).body(response);
    }

    /**
     * Delete comment from blog
     * DELETE /api/blogs/:id/comments/:commentIndex
     */
    @DeleteMapping("/{id}/comments/{commentIndex}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Blog>> deleteComment(
            @PathVariable String id,
            @PathVariable int commentIndex,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userService.findByEmail(userDetails.getUsername());
        Blog blog = blogService.deleteComment(id, commentIndex, currentUser);

        ApiResponse<Blog> response = ApiResponse.success(
                "Comment deleted successfully",
                blog
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get blog categories with count
     * GET /api/blogs/categories/list
     */
    @GetMapping("/categories/list")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCategories() {
        List<Map<String, Object>> categories = blogService.getCategories();

        ApiResponse<List<Map<String, Object>>> response = ApiResponse.success(
                "Categories retrieved successfully",
                categories
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get popular tags
     * GET /api/blogs/tags/popular
     */
    @GetMapping("/tags/popular")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPopularTags(
            @RequestParam(defaultValue = "20") int limit
    ) {
        List<Map<String, Object>> tags = blogService.getPopularTags(limit);

        ApiResponse<List<Map<String, Object>>> response = ApiResponse.success(
                "Tags retrieved successfully",
                tags
        );

        return ResponseEntity.ok(response);
    }
}

