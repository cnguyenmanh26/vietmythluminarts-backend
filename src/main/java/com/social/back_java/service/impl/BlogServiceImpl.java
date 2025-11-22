package com.social.back_java.service.impl;

import com.social.back_java.model.Blog;
import com.social.back_java.model.Comment;
import com.social.back_java.model.ProductImage;
import com.social.back_java.model.User;
import com.social.back_java.repository.BlogRepository;
import com.social.back_java.repository.UserRepository;
import com.social.back_java.service.IBlogService;
import com.social.back_java.service.ICloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BlogServiceImpl implements IBlogService {

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ICloudinaryService cloudinaryService;

    @Override
    public List<Blog> getAllBlogs() {
        return blogRepository.findAll();
    }

    @Override
    public Optional<Blog> getBlogById(Long id) {
        return blogRepository.findById(id);
    }

    @Override
    public Optional<Blog> getBlogBySlug(String slug) {
        return blogRepository.findBySlug(slug);
    }

    @Override
    public List<Blog> getBlogsByCategory(String category) {
        return blogRepository.findByCategory(category);
    }

    @Override
    public List<Blog> getBlogsByStatus(String status) {
        return blogRepository.findByStatus(status);
    }

    @Override
    public Blog createBlog(Blog blog) {
        return blogRepository.save(blog);
    }

    @Override
    public Blog updateBlog(Long id, Blog blog) {
        if (blogRepository.existsById(id)) {
            blog.setId(id);
            return blogRepository.save(blog);
        }
        throw new RuntimeException("Blog not found with id: " + id);
    }

    @Override
    public void deleteBlog(Long id) {
        blogRepository.deleteById(id);
    }

    @Override
    public Blog publishBlog(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + id));
        blog.setStatus("published");
        blog.setPublishedAt(new Date());
        return blogRepository.save(blog);
    }

    @Override
    public Blog addComment(Long blogId, Comment comment) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + blogId));
        blog.getComments().add(comment);
        return blogRepository.save(blog);
    }

    @Override
    public Blog likeBlog(Long blogId, Long userId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + blogId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Check if user already liked - if yes, unlike
        boolean alreadyLiked = blog.getLikes().removeIf(u -> u.getId().equals(userId));
        
        if (!alreadyLiked) {
            // Like
            blog.getLikes().add(user);
        }
        
        return blogRepository.save(blog);
    }

    @Override
    public Blog unlikeBlog(Long blogId, Long userId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + blogId));
        
        blog.getLikes().removeIf(u -> u.getId().equals(userId));
        return blogRepository.save(blog);
    }

    @Override
    public Blog incrementViews(Long blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + blogId));
        
        blog.setViews(blog.getViews() + 1);
        return blogRepository.save(blog);
    }

    @Override
    public Blog addImagesToBlog(Long blogId, List<MultipartFile> images) throws IOException {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + blogId));

        for (MultipartFile image : images) {
            Map<String, Object> uploadResult = cloudinaryService.uploadImage(image);
            
            ProductImage blogImage = new ProductImage();
            blogImage.setUrl((String) uploadResult.get("secure_url"));
            blogImage.setPublicId((String) uploadResult.get("public_id"));
            blogImage.setAlt(blog.getTitle());
            blogImage.setPrimary(blog.getImages().isEmpty()); // First image is primary
            
            blog.getImages().add(blogImage);
        }

        return blogRepository.save(blog);
    }

    @Override
    public Blog removeImageFromBlog(Long blogId, String publicId) throws IOException {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + blogId));

        blog.getImages().removeIf(image -> image.getPublicId().equals(publicId));
        cloudinaryService.deleteFile(publicId);

        return blogRepository.save(blog);
    }
}
