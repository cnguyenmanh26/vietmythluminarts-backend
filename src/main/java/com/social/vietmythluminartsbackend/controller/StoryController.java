package com.social.vietmythluminartsbackend.controller;

import com.social.vietmythluminartsbackend.dto.response.ApiResponse;
import com.social.vietmythluminartsbackend.model.Story;
import com.social.vietmythluminartsbackend.model.User;
import com.social.vietmythluminartsbackend.model.enums.BlogStatus;
import com.social.vietmythluminartsbackend.service.StoryService;
import com.social.vietmythluminartsbackend.service.UserService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for story endpoints
 * Migrated from Node.js storyController.js
 * 
 * Base URL: /api/stories
 */
@Slf4j
@RestController
@RequestMapping("/api/stories")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;
    private final UserService userService;

    /**
     * Get all stories with pagination and filtering
     * GET /api/stories
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Story>>> getStories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) BlogStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tags,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Pageable pageable = PageRequest.of(page, limit, Sort.by("sortOrder", "publishedAt").descending());

        List<String> tagList = null;
        if (tags != null && !tags.isEmpty()) {
            tagList = List.of(tags.split(","));
        }

        User currentUser = null;
        if (userDetails != null) {
            try {
                currentUser = userService.findByEmail(userDetails.getUsername());
            } catch (Exception e) {
                // Continue as public
            }
        }

        Page<Story> stories = storyService.getStories(pageable, status, search, tagList, currentUser);

        ApiResponse<Page<Story>> response = ApiResponse.success(
                "Stories retrieved successfully",
                stories
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get story by ID or slug
     * GET /api/stories/:identifier
     */
    @GetMapping("/{identifier}")
    public ResponseEntity<ApiResponse<Story>> getStoryByIdentifier(
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

        Story story = storyService.getStoryByIdentifier(identifier, currentUser);

        ApiResponse<Story> response = ApiResponse.success(
                "Story retrieved successfully",
                story
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get popular tags
     * GET /api/stories/tags/popular
     */
    @GetMapping("/tags/popular")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPopularTags(
            @RequestParam(defaultValue = "20") int limit
    ) {
        List<Map<String, Object>> tags = storyService.getPopularTags(limit);

        ApiResponse<List<Map<String, Object>>> response = ApiResponse.success(
                "Tags retrieved successfully",
                tags
        );

        return ResponseEntity.ok(response);
    }
}

