package com.social.vietmythluminartsbackend.controller;

import com.social.vietmythluminartsbackend.dto.request.CreateProductRequest;
import com.social.vietmythluminartsbackend.dto.request.UpdateProductRequest;
import com.social.vietmythluminartsbackend.dto.response.ApiResponse;
import com.social.vietmythluminartsbackend.model.Product;
import com.social.vietmythluminartsbackend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST Controller for product endpoints
 * Migrated from Node.js productController.js
 * 
 * Base URL: /api/products
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Get all products with pagination
     * GET /api/products
     * 
     * Query params: page, limit, sortBy, sortDir, category, search
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Product>>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, limit, sort);

        Page<Product> products;
        if (search != null && !search.isEmpty()) {
            products = productService.searchProducts(search, pageable);
        } else if (category != null && !category.isEmpty()) {
            products = productService.getProductsByCategory(category, pageable);
        } else {
            products = productService.getAllProducts(pageable);
        }

        ApiResponse<Page<Product>> response = ApiResponse.success(
                "Products retrieved successfully",
                products
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get product by ID
     * GET /api/products/:id
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable String id) {
        Product product = productService.getProductById(id);
        
        ApiResponse<Product> response = ApiResponse.success(
                "Product retrieved successfully",
                product
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get products on sale
     * GET /api/products/sale
     */
    @GetMapping("/sale")
    public ResponseEntity<ApiResponse<Page<Product>>> getProductsOnSale(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Product> products = productService.getProductsOnSale(pageable);

        ApiResponse<Page<Product>> response = ApiResponse.success(
                "Products on sale retrieved successfully",
                products
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get all categories
     * GET /api/products/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        List<String> categories = productService.getAllCategories();

        ApiResponse<List<String>> response = ApiResponse.success(
                "Categories retrieved successfully",
                categories
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Create new product (Admin only)
     * POST /api/products
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Product>> createProduct(
            @Valid @RequestBody CreateProductRequest request
    ) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .pricegiamgia(request.getPricegiamgia())
                .category(request.getCategory())
                .stock(request.getStock() != null ? request.getStock() : 0)
                .youtubeUrl(request.getYoutubeUrl())
                .build();

        Product createdProduct = productService.createProduct(product);

        ApiResponse<Product> response = ApiResponse.success(
                "Product created successfully",
                createdProduct
        );

        return ResponseEntity.status(201).body(response);
    }

    /**
     * Update product (Admin only)
     * PUT /api/products/:id
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        Product productUpdate = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .pricegiamgia(request.getPricegiamgia())
                .category(request.getCategory())
                .stock(request.getStock())
                .youtubeUrl(request.getYoutubeUrl())
                .build();

        Product updatedProduct = productService.updateProduct(id, productUpdate);

        ApiResponse<Product> response = ApiResponse.success(
                "Product updated successfully",
                updatedProduct
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Delete product (Admin only)
     * DELETE /api/products/:id
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);

        ApiResponse<Void> response = ApiResponse.success(
                "Product deleted successfully",
                null
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Upload product images (Admin only)
     * POST /api/products/:id/images
     */
    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Product>> uploadProductImages(
            @PathVariable String id,
            @RequestParam("images") List<MultipartFile> files
    ) {
        try {
            Product updatedProduct = productService.uploadProductImages(id, files);

            ApiResponse<Product> response = ApiResponse.success(
                    "Images uploaded successfully",
                    updatedProduct
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to upload images: {}", e.getMessage());
            throw new RuntimeException("Failed to upload images: " + e.getMessage());
        }
    }

    /**
     * Upload product video (Admin only)
     * POST /api/products/:id/video
     */
    @PostMapping("/{id}/video")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Product>> uploadProductVideo(
            @PathVariable String id,
            @RequestParam("video") MultipartFile file
    ) {
        try {
            Product updatedProduct = productService.uploadProductVideo(id, file);

            ApiResponse<Product> response = ApiResponse.success(
                    "Video uploaded successfully",
                    updatedProduct
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to upload video: {}", e.getMessage());
            throw new RuntimeException("Failed to upload video: " + e.getMessage());
        }
    }

    /**
     * Delete product image (Admin only)
     * DELETE /api/products/:id/images/:publicId
     */
    @DeleteMapping("/{id}/images/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Product>> deleteProductImage(
            @PathVariable String id,
            @PathVariable String publicId
    ) {
        Product updatedProduct = productService.deleteProductImage(id, publicId);

        ApiResponse<Product> response = ApiResponse.success(
                "Image deleted successfully",
                updatedProduct
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Set primary image (Admin only)
     * PUT /api/products/:id/images/:publicId/primary
     */
    @PutMapping("/{id}/images/{publicId}/primary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Product>> setPrimaryImage(
            @PathVariable String id,
            @PathVariable String publicId
    ) {
        Product updatedProduct = productService.setPrimaryImage(id, publicId);

        ApiResponse<Product> response = ApiResponse.success(
                "Primary image updated successfully",
                updatedProduct
        );

        return ResponseEntity.ok(response);
    }
}

