package com.social.vietmythluminartsbackend.dto.request;

import com.social.vietmythluminartsbackend.model.enums.BlogCategory;
import com.social.vietmythluminartsbackend.model.enums.BlogStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating a blog
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBlogRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 10, max = 200, message = "Title must be between 10 and 200 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min = 100, message = "Content must be at least 100 characters")
    private String content; // No max - supports very long blog posts

    @Size(max = 500, message = "Excerpt must not exceed 500 characters")
    private String excerpt;

    @NotBlank(message = "Category is required")
    private BlogCategory category;

    private List<String> tags; // Max 10

    private BlogStatus status;

    @Size(max = 70, message = "Meta title must not exceed 70 characters")
    private String metaTitle;

    @Size(max = 160, message = "Meta description must not exceed 160 characters")
    private String metaDescription;
}

