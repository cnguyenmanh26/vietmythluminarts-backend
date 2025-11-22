package com.social.back_java.controller;

import com.social.back_java.model.Blog;
import com.social.back_java.model.Comment;
import com.social.back_java.service.IBlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/blogs")
@CrossOrigin(origins = "*")
public class BlogController {

    @Autowired
    private IBlogService blogService;

    @GetMapping
    public ResponseEntity<List<Blog>> getAllBlogs() {
        return ResponseEntity.ok(blogService.getAllBlogs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Blog> getBlogById(@PathVariable Long id) {
        Optional<Blog> blog = blogService.getBlogById(id);
        if (blog.isPresent()) {
            // Increment views
            blogService.incrementViews(id);
            return ResponseEntity.ok(blog.get());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Blog> getBlogBySlug(@PathVariable String slug) {
        return blogService.getBlogBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Blog>> getBlogsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(blogService.getBlogsByCategory(category));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Blog>> getBlogsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(blogService.getBlogsByStatus(status));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Blog> createBlog(
            @RequestParam(value = "title", required = true) String title,
            @RequestParam(value = "content", required = true) String content,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "tags", required = false) String tagsJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        try {
            Blog blog = new Blog();
            blog.setTitle(title);
            blog.setContent(content);
            blog.setCategory(category);
            blog.setStatus(status != null ? status : "draft");
            
            // Parse tags JSON if provided
            if (tagsJson != null && !tagsJson.isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String[] tags = mapper.readValue(tagsJson, String[].class);
                blog.setTags(java.util.Arrays.asList(tags));
            }
            
            Blog createdBlog = blogService.createBlog(blog);
            
            // Upload images if provided
            if (images != null && !images.isEmpty()) {
                createdBlog = blogService.addImagesToBlog(createdBlog.getId(), images);
            }
            
            return ResponseEntity.ok(createdBlog);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Blog> updateBlog(
            @PathVariable Long id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "tags", required = false) String tagsJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        try {
            Blog blog = blogService.getBlogById(id)
                    .orElseThrow(() -> new RuntimeException("Blog not found"));
            
            // Update fields if provided
            if (title != null) blog.setTitle(title);
            if (content != null) blog.setContent(content);
            if (category != null) blog.setCategory(category);
            if (status != null) blog.setStatus(status);
            
            // Parse tags JSON if provided
            if (tagsJson != null && !tagsJson.isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String[] tags = mapper.readValue(tagsJson, String[].class);
                blog.setTags(java.util.Arrays.asList(tags));
            }
            
            Blog updatedBlog = blogService.updateBlog(id, blog);
            
            // Upload new images if provided
            if (images != null && !images.isEmpty()) {
                updatedBlog = blogService.addImagesToBlog(id, images);
            }
            
            return ResponseEntity.ok(updatedBlog);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        blogService.deleteBlog(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Blog> publishBlog(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(blogService.publishBlog(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<Blog> addComment(@PathVariable Long id, @RequestBody Comment comment) {
        try {
            return ResponseEntity.ok(blogService.addComment(id, comment));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{blogId}/like/{userId}")
    public ResponseEntity<Blog> likeBlog(@PathVariable Long blogId, @PathVariable Long userId) {
        try {
            return ResponseEntity.ok(blogService.likeBlog(blogId, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{blogId}/like/{userId}")
    public ResponseEntity<Blog> unlikeBlog(@PathVariable Long blogId, @PathVariable Long userId) {
        try {
            return ResponseEntity.ok(blogService.unlikeBlog(blogId, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Cloudinary endpoints
    @PostMapping("/{id}/images")
    public ResponseEntity<Blog> uploadImages(
            @PathVariable Long id,
            @RequestParam("images") List<MultipartFile> images) {
        try {
            Blog blog = blogService.addImagesToBlog(id, images);
            return ResponseEntity.ok(blog);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}/images/{publicId}")
    public ResponseEntity<Blog> deleteImage(
            @PathVariable Long id,
            @PathVariable String publicId) {
        try {
            Blog blog = blogService.removeImageFromBlog(id, publicId);
            return ResponseEntity.ok(blog);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
