package com.social.vietmythluminartsbackend.service;

import com.social.vietmythluminartsbackend.exception.ResourceNotFoundException;
import com.social.vietmythluminartsbackend.model.Product;
import com.social.vietmythluminartsbackend.model.embedded.ProductImage;
import com.social.vietmythluminartsbackend.model.embedded.ProductVideo;
import com.social.vietmythluminartsbackend.model.embedded.StoryBlock;
import com.social.vietmythluminartsbackend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for product operations
 * Migrated from Node.js productController.js
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;

    /**
     * Get all products with pagination
     */
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    /**
     * Get product by ID
     */
    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    /**
     * Get products by category
     */
    public Page<Product> getProductsByCategory(String category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable);
    }

    /**
     * Get products on sale
     */
    public Page<Product> getProductsOnSale(Pageable pageable) {
        return productRepository.findProductsOnSale(pageable);
    }

    /**
     * Search products by name or description
     */
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                keyword, keyword, pageable
        );
    }

    /**
     * Create new product
     */
    @Transactional
    public Product createProduct(Product product) {
        // Validate
        validateProduct(product);

        // Save product
        Product savedProduct = productRepository.save(product);
        log.info("Product created: {}", savedProduct.getId());

        return savedProduct;
    }

    /**
     * Upload images for product
     */
    @Transactional
    public Product uploadProductImages(String productId, List<MultipartFile> files) throws IOException {
        Product product = getProductById(productId);

        // Check max images limit
        int currentImageCount = product.getImages().size();
        if (currentImageCount + files.size() > 10) {
            throw new IllegalArgumentException(
                    String.format("Maximum 10 images allowed. Current: %d, Uploading: %d", 
                            currentImageCount, files.size())
            );
        }

        // Upload images
        List<Map<String, Object>> uploadResults = cloudinaryService.uploadMultipleImages(files, "products");

        // Add to product
        for (int i = 0; i < uploadResults.size(); i++) {
            Map<String, Object> result = uploadResults.get(i);
            ProductImage image = ProductImage.builder()
                    .url((String) result.get("url"))
                    .publicId((String) result.get("publicId"))
                    .alt(product.getName() + " - Image " + (currentImageCount + i + 1))
                    .isPrimary(product.getImages().isEmpty() && i == 0)
                    .build();
            product.getImages().add(image);
        }

        return productRepository.save(product);
    }

    /**
     * Upload video for product
     */
    @Transactional
    public Product uploadProductVideo(String productId, MultipartFile file) throws IOException {
        Product product = getProductById(productId);

        // Check max videos limit
        if (product.getVideos().size() >= 5) {
            throw new IllegalArgumentException("Maximum 5 videos allowed");
        }

        // Upload video
        Map<String, Object> uploadResult = cloudinaryService.uploadVideo(file, "products");

        // Add to product
        ProductVideo video = ProductVideo.builder()
                .url((String) uploadResult.get("url"))
                .publicId((String) uploadResult.get("publicId"))
                .thumbnail((String) uploadResult.get("thumbnail"))
                .duration((Double) uploadResult.get("duration"))
                .build();
        product.getVideos().add(video);

        return productRepository.save(product);
    }

    /**
     * Update product
     */
    @Transactional
    public Product updateProduct(String id, Product productUpdate) {
        Product product = getProductById(id);

        // Update fields
        if (productUpdate.getName() != null) {
            product.setName(productUpdate.getName());
        }
        if (productUpdate.getDescription() != null) {
            product.setDescription(productUpdate.getDescription());
        }
        if (productUpdate.getPrice() != null) {
            product.setPrice(productUpdate.getPrice());
        }
        if (productUpdate.getPricegiamgia() != null) {
            product.setPricegiamgia(productUpdate.getPricegiamgia());
        }
        if (productUpdate.getCategory() != null) {
            product.setCategory(productUpdate.getCategory());
        }
        if (productUpdate.getStock() != null) {
            product.setStock(productUpdate.getStock());
        }
        if (productUpdate.getYoutubeUrl() != null) {
            product.setYoutubeUrl(productUpdate.getYoutubeUrl());
        }
        if (productUpdate.getStoryBlocks() != null) {
            product.setStoryBlocks(productUpdate.getStoryBlocks());
        }

        return productRepository.save(product);
    }

    /**
     * Delete product
     */
    @Transactional
    public void deleteProduct(String id) {
        Product product = getProductById(id);

        // Delete all images from Cloudinary
        if (product.getImages() != null) {
            List<String> imagePublicIds = product.getImages().stream()
                    .map(ProductImage::getPublicId)
                    .toList();
            cloudinaryService.deleteMultipleImages(imagePublicIds);
        }

        // Delete all videos from Cloudinary
        if (product.getVideos() != null) {
            product.getVideos().forEach(video -> 
                    cloudinaryService.deleteVideo(video.getPublicId()));
        }

        // Delete story block images
        if (product.getStoryBlocks() != null) {
            product.getStoryBlocks().stream()
                    .filter(block -> "image".equals(block.getType()))
                    .filter(block -> block.getImage() != null)
                    .forEach(block -> cloudinaryService.deleteMedia(
                            block.getImage().getPublicId(), "image"));
        }

        productRepository.deleteById(id);
        log.info("Product deleted: {}", id);
    }

    /**
     * Delete product image
     */
    @Transactional
    public Product deleteProductImage(String productId, String publicId) {
        Product product = getProductById(productId);

        // Find and remove image
        boolean removed = product.getImages().removeIf(img -> publicId.equals(img.getPublicId()));

        if (!removed) {
            throw new ResourceNotFoundException("Image not found with publicId: " + publicId);
        }

        // Delete from Cloudinary
        cloudinaryService.deleteMedia(publicId, "image");

        // If we removed the primary image, make first image primary
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            boolean hasPrimary = product.getImages().stream()
                    .anyMatch(img -> Boolean.TRUE.equals(img.getIsPrimary()));
            
            if (!hasPrimary) {
                product.getImages().get(0).setIsPrimary(true);
            }
        }

        return productRepository.save(product);
    }

    /**
     * Set primary image
     */
    @Transactional
    public Product setPrimaryImage(String productId, String publicId) {
        Product product = getProductById(productId);

        // Find the image
        ProductImage targetImage = product.getImages().stream()
                .filter(img -> publicId.equals(img.getPublicId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with publicId: " + publicId));

        // Reset all to non-primary
        product.getImages().forEach(img -> img.setIsPrimary(false));

        // Set target as primary
        targetImage.setIsPrimary(true);

        return productRepository.save(product);
    }

    /**
     * Get all distinct categories
     */
    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }

    // ==================== Helper Methods ====================

    /**
     * Validate product data
     */
    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }

        if (product.getPrice() == null || product.getPrice() < 0) {
            throw new IllegalArgumentException("Valid price is required");
        }

        if (product.getPricegiamgia() != null && product.getPricegiamgia() > product.getPrice()) {
            throw new IllegalArgumentException("Discounted price cannot be greater than regular price");
        }

        if (product.getStock() != null && product.getStock() < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
    }
}

