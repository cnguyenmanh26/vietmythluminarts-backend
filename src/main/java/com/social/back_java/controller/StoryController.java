package com.social.back_java.controller;

import com.social.back_java.model.Story;
import com.social.back_java.service.IStoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/stories")
@CrossOrigin(origins = "*")
public class StoryController {

    @Autowired
    private IStoryService storyService;

    @GetMapping
    public ResponseEntity<List<Story>> getAllStories() {
        return ResponseEntity.ok(storyService.getAllStories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Story> getStoryById(@PathVariable Long id) {
        return storyService.getStoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Story> getStoryBySlug(@PathVariable String slug) {
        return storyService.getStoryBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Story>> getStoriesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(storyService.getStoriesByStatus(status));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Story> createStory(
            @RequestParam(value = "story", required = false) String storyJson,
            @RequestPart(value = "featuredImage", required = false) MultipartFile featuredImage) {
        try {
            if (storyJson == null || storyJson.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Story story = mapper.readValue(storyJson, Story.class);
            
            Story createdStory = storyService.createStory(story);
            
            // Upload featured image if provided
            if (featuredImage != null && !featuredImage.isEmpty()) {
                createdStory = storyService.uploadFeaturedImage(createdStory.getId(), featuredImage);
            }
            
            return ResponseEntity.ok(createdStory);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Story> updateStory(
            @PathVariable Long id,
            @RequestParam(value = "story", required = false) String storyJson,
            @RequestPart(value = "featuredImage", required = false) MultipartFile featuredImage) {
        try {
            Story story = null;
            if (storyJson != null && !storyJson.isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                story = mapper.readValue(storyJson, Story.class);
            }
            
            Story updatedStory = storyService.updateStory(id, story);
            
            // Upload new featured image if provided
            if (featuredImage != null && !featuredImage.isEmpty()) {
                updatedStory = storyService.uploadFeaturedImage(id, featuredImage);
            }
            
            return ResponseEntity.ok(updatedStory);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStory(@PathVariable Long id) {
        storyService.deleteStory(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Story> publishStory(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(storyService.publishStory(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Cloudinary endpoints
    @PostMapping("/{id}/featured-image")
    public ResponseEntity<Story> uploadFeaturedImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image) {
        try {
            Story story = storyService.uploadFeaturedImage(id, image);
            return ResponseEntity.ok(story);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/blocks/{blockIndex}/image")
    public ResponseEntity<Story> uploadBlockImage(
            @PathVariable Long id,
            @PathVariable int blockIndex,
            @RequestParam("image") MultipartFile image) {
        try {
            Story story = storyService.addBlockImage(id, blockIndex, image);
            return ResponseEntity.ok(story);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}/blocks/{blockIndex}/image")
    public ResponseEntity<Story> deleteBlockImage(
            @PathVariable Long id,
            @PathVariable int blockIndex) {
        try {
            Story story = storyService.removeBlockImage(id, blockIndex);
            return ResponseEntity.ok(story);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
