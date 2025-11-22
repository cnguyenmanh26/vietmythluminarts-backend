package com.social.back_java.service;

import com.social.back_java.model.Story;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface IStoryService {
    List<Story> getAllStories();
    Optional<Story> getStoryById(Long id);
    Optional<Story> getStoryBySlug(String slug);
    List<Story> getStoriesByStatus(String status);
    Story createStory(Story story);
    Story updateStory(Long id, Story story);
    void deleteStory(Long id);
    Story publishStory(Long id);
    
    // Cloudinary methods
    Story uploadFeaturedImage(Long storyId, MultipartFile image) throws IOException;
    Story addBlockImage(Long storyId, int blockIndex, MultipartFile image) throws IOException;
    Story removeBlockImage(Long storyId, int blockIndex) throws IOException;
}
