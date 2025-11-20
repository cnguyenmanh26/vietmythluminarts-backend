package com.social.vietmythluminartsbackend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a product
 * All fields are optional
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Min(value = 0, message = "Price cannot be negative")
    private Double price;

    @Min(value = 0, message = "Discounted price cannot be negative")
    private Double pricegiamgia;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    private String youtubeUrl;
}

