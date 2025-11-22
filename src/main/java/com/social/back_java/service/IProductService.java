package com.social.back_java.service;

import com.social.back_java.model.Product;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface IProductService {
    List<Product> getAllProducts();
    Optional<Product> getProductById(Long id);
    List<Product> getProductsByCategory(String category);
    List<Product> searchProducts(String name);
    Product createProduct(Product product);
    Product updateProduct(Long id, Product product);
    void deleteProduct(Long id);
    
    // Cloudinary methods
    Product addImagesToProduct(Long productId, List<MultipartFile> images) throws IOException;
    Product addVideosToProduct(Long productId, List<MultipartFile> videos) throws IOException;
    Product removeImageFromProduct(Long productId, String publicId) throws IOException;
    Product removeVideoFromProduct(Long productId, String publicId) throws IOException;
}

