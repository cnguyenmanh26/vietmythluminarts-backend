package com.social.back_java.controller;

import com.social.back_java.model.Product;
import com.social.back_java.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private IProductService productService;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String name) {
        return ResponseEntity.ok(productService.searchProducts(name));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Product> createProduct(
            @RequestParam(value = "name", required = true) String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "price", required = true) Double price,
            @RequestParam(value = "pricegiamgia", required = false) Double pricegiamgia,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "stock", required = true) Integer stock,
            @RequestParam(value = "youtubeUrl", required = false) String youtubeUrl,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "videos", required = false) List<MultipartFile> videos) {
        try {
            // Build product object
            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setPricegiamgia(pricegiamgia);
            product.setCategory(category);
            product.setStock(stock);
            product.setYoutubeUrl(youtubeUrl);
            
            Product createdProduct = productService.createProduct(product);
            
            // Upload images if provided
            if (images != null && !images.isEmpty()) {
                createdProduct = productService.addImagesToProduct(createdProduct.getId(), images);
            }
            
            // Upload videos if provided
            if (videos != null && !videos.isEmpty()) {
                createdProduct = productService.addVideosToProduct(createdProduct.getId(), videos);
            }
            
            return ResponseEntity.ok(createdProduct);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "price", required = false) Double price,
            @RequestParam(value = "pricegiamgia", required = false) Double pricegiamgia,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "stock", required = false) Integer stock,
            @RequestParam(value = "youtubeUrl", required = false) String youtubeUrl,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "videos", required = false) List<MultipartFile> videos) {
        try {
            // Get existing product
            Product product = productService.getProductById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            
            // Update fields if provided
            if (name != null) product.setName(name);
            if (description != null) product.setDescription(description);
            if (price != null) product.setPrice(price);
            if (pricegiamgia != null) product.setPricegiamgia(pricegiamgia);
            if (category != null) product.setCategory(category);
            if (stock != null) product.setStock(stock);
            if (youtubeUrl != null) product.setYoutubeUrl(youtubeUrl);
            
            Product updatedProduct = productService.updateProduct(id, product);
            
            // Upload new images if provided
            if (images != null && !images.isEmpty()) {
                updatedProduct = productService.addImagesToProduct(id, images);
            }
            
            // Upload new videos if provided
            if (videos != null && !videos.isEmpty()) {
                updatedProduct = productService.addVideosToProduct(id, videos);
            }
            
            return ResponseEntity.ok(updatedProduct);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    // Cloudinary endpoints
    @PostMapping("/{id}/images")
    public ResponseEntity<Product> uploadImages(
            @PathVariable Long id,
            @RequestParam("images") List<MultipartFile> images) {
        try {
            Product product = productService.addImagesToProduct(id, images);
            return ResponseEntity.ok(product);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/videos")
    public ResponseEntity<Product> uploadVideos(
            @PathVariable Long id,
            @RequestParam("videos") List<MultipartFile> videos) {
        try {
            Product product = productService.addVideosToProduct(id, videos);
            return ResponseEntity.ok(product);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}/images/{publicId}")
    public ResponseEntity<Product> deleteImage(
            @PathVariable Long id,
            @PathVariable String publicId) {
        try {
            Product product = productService.removeImageFromProduct(id, publicId);
            return ResponseEntity.ok(product);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}/videos/{publicId}")
    public ResponseEntity<Product> deleteVideo(
            @PathVariable Long id,
            @PathVariable String publicId) {
        try {
            Product product = productService.removeVideoFromProduct(id, publicId);
            return ResponseEntity.ok(product);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
