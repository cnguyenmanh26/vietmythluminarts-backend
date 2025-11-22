package com.social.back_java.service;

import com.social.back_java.model.Blog;
import com.social.back_java.model.Comment;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface IBlogService {
    List<Blog> getAllBlogs();
    Optional<Blog> getBlogById(Long id);
    Optional<Blog> getBlogBySlug(String slug);
    List<Blog> getBlogsByCategory(String category);
    List<Blog> getBlogsByStatus(String status);
    Blog createBlog(Blog blog);
    Blog updateBlog(Long id, Blog blog);
    void deleteBlog(Long id);
    Blog publishBlog(Long id);
    Blog addComment(Long blogId, Comment comment);
    Blog likeBlog(Long blogId, Long userId);
    Blog unlikeBlog(Long blogId, Long userId);
    Blog incrementViews(Long blogId);
    
    // Cloudinary methods
    Blog addImagesToBlog(Long blogId, List<MultipartFile> images) throws IOException;
    Blog removeImageFromBlog(Long blogId, String publicId) throws IOException;
}
