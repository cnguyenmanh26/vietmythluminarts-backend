package com.social.vietmythluminartsbackend.model;

import com.social.vietmythluminartsbackend.model.embedded.ProductImage;
import com.social.vietmythluminartsbackend.model.embedded.ProductVideo;
import com.social.vietmythluminartsbackend.model.embedded.StoryBlock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Product entity for wooden toys
 * Migrated from Node.js Product model (WoodToy backend)
 * 
 * Features:
 * - Multiple images with primary image support
 * - Multiple videos with thumbnails
 * - Rich product stories with text and image blocks
 * - Price and discounted price
 * - Stock management
 * - YouTube video integration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {

    @Id
    private String id;

    /**
     * Product name
     * Required, max 200 characters
     * Indexed for text search
     */
    @TextIndexed(weight = 10)
    private String name;

    /**
     * Product description (short)
     * Max 2000 characters
     * Indexed for text search
     */
    @TextIndexed(weight = 5)
    private String description;

    /**
     * Regular price
     * Required
     */
    private Double price;

    /**
     * Discounted price (optional)
     * If set, this will be displayed as the sale price
     */
    private Double pricegiamgia;

    /**
     * Product category
     * E.g., "Puzzles", "Building Blocks", "Educational Toys"
     */
    @TextIndexed(weight = 3)
    private String category;

    /**
     * Available stock quantity
     * Default: 0
     */
    @Builder.Default
    private Integer stock = 0;

    /**
     * Product images (max 10)
     * First image or image with isPrimary=true is the featured image
     */
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    /**
     * Product videos (max 5)
     * Auto-generated thumbnails
     */
    @Builder.Default
    private List<ProductVideo> videos = new ArrayList<>();

    /**
     * YouTube video URL (optional)
     * Alternative to uploaded videos
     */
    private String youtubeUrl;

    /**
     * Rich product story with interleaved text and images
     * Max 50 blocks
     * Replaces legacy 'story' text field
     */
    @Builder.Default
    private List<StoryBlock> storyBlocks = new ArrayList<>();

    /**
     * Product creation timestamp
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ==================== Business Logic Methods ====================

    /**
     * Get primary image URL
     * Priority: image with isPrimary=true → first image → null
     */
    public String getPrimaryImage() {
        if (images == null || images.isEmpty()) {
            return null;
        }

        // Find image with isPrimary=true
        return images.stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .findFirst()
                .map(ProductImage::getUrl)
                .orElseGet(() -> images.get(0).getUrl());
    }

    /**
     * Get effective price (discounted if available, otherwise regular price)
     */
    public Double getEffectivePrice() {
        return (pricegiamgia != null && pricegiamgia > 0) ? pricegiamgia : price;
    }

    /**
     * Check if product is on sale
     */
    public boolean isOnSale() {
        return pricegiamgia != null && pricegiamgia > 0 && pricegiamgia < price;
    }

    /**
     * Check if product is in stock
     */
    public boolean isInStock() {
        return stock != null && stock > 0;
    }

    /**
     * Check if product has sufficient stock
     */
    public boolean hasSufficientStock(int requestedQuantity) {
        return stock != null && stock >= requestedQuantity;
    }

    /**
     * Reduce stock by quantity
     */
    public void reduceStock(int quantity) {
        if (stock == null || stock < quantity) {
            throw new IllegalStateException("Insufficient stock");
        }
        this.stock -= quantity;
    }

    /**
     * Increase stock by quantity
     */
    public void increaseStock(int quantity) {
        if (this.stock == null) {
            this.stock = 0;
        }
        this.stock += quantity;
    }
}

