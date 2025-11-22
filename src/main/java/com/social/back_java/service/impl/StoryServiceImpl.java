package com.social.back_java.service.impl;

import com.social.back_java.model.Story;
import com.social.back_java.model.StoryBlock;
import com.social.back_java.repository.StoryRepository;
import com.social.back_java.service.ICloudinaryService;
import com.social.back_java.service.IStoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StoryServiceImpl implements IStoryService {

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private ICloudinaryService cloudinaryService;

    @Override
    public List<Story> getAllStories() {
        return storyRepository.findAll();
    }

    @Override
    public Optional<Story> getStoryById(Long id) {
        return storyRepository.findById(id);
    }

    @Override
    public Optional<Story> getStoryBySlug(String slug) {
        return storyRepository.findBySlug(slug);
    }

    @Override
    public List<Story> getStoriesByStatus(String status) {
        return storyRepository.findByStatus(status);
    }

    @Override
    public Story createStory(Story story) {
        return storyRepository.save(story);
    }

    @Override
    public Story updateStory(Long id, Story story) {
        if (storyRepository.existsById(id)) {
            story.setId(id);
            return storyRepository.save(story);
        }
        throw new RuntimeException("Story not found with id: " + id);
    }

    @Override
    public void deleteStory(Long id) {
        storyRepository.deleteById(id);
    }

    @Override
    public Story publishStory(Long id) {
        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story not found with id: " + id));
        story.setStatus("published");
        story.setPublishedAt(new Date());
        return storyRepository.save(story);
    }

    @Override
    public Story uploadFeaturedImage(Long storyId, MultipartFile image) throws IOException {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found with id: " + storyId));

        Map<String, Object> uploadResult = cloudinaryService.uploadImage(image);
        story.setFeaturedImage((String) uploadResult.get("secure_url"));

        return storyRepository.save(story);
    }

    @Override
    public Story addBlockImage(Long storyId, int blockIndex, MultipartFile image) throws IOException {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found with id: " + storyId));

        if (blockIndex < 0 || blockIndex >= story.getBlocks().size()) {
            throw new RuntimeException("Invalid block index: " + blockIndex);
        }

        Map<String, Object> uploadResult = cloudinaryService.uploadImage(image);
        
        StoryBlock block = story.getBlocks().get(blockIndex);
        block.setType("image");
        block.setImageUrl((String) uploadResult.get("secure_url"));
        block.setImagePublicId((String) uploadResult.get("public_id"));

        return storyRepository.save(story);
    }

    @Override
    public Story removeBlockImage(Long storyId, int blockIndex) throws IOException {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found with id: " + storyId));

        if (blockIndex < 0 || blockIndex >= story.getBlocks().size()) {
            throw new RuntimeException("Invalid block index: " + blockIndex);
        }

        StoryBlock block = story.getBlocks().get(blockIndex);
        if (block.getImagePublicId() != null) {
            cloudinaryService.deleteFile(block.getImagePublicId());
        }

        block.setImageUrl(null);
        block.setImagePublicId(null);
        block.setImageCaption(null);
        block.setImageAlt(null);

        return storyRepository.save(story);
    }
}
