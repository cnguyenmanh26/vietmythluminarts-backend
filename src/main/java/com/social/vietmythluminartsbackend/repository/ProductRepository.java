package com.social.vietmythluminartsbackend.repository;

import com.social.vietmythluminartsbackend.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Product entity
 */
@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    /**
     * Find products by category
     */
    Page<Product> findByCategory(String category, Pageable pageable);

    /**
     * Find products by category
     */
    List<Product> findByCategory(String category);

    /**
     * Find products with stock greater than 0
     */
    Page<Product> findByStockGreaterThan(Integer stock, Pageable pageable);

    /**
     * Find products with discounted price (on sale)
     */
    @Query("{ 'pricegiamgia': { $exists: true, $ne: null, $gt: 0 } }")
    Page<Product> findProductsOnSale(Pageable pageable);

    /**
     * Find products by price range
     */
    Page<Product> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);

    /**
     * Search products by name or description (text search)
     */
    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String description, Pageable pageable
    );

    /**
     * Count products by category
     */
    long countByCategory(String category);

    /**
     * Find all distinct categories
     */
    @Query(value = "{}", fields = "{ 'category': 1 }")
    List<String> findAllCategories();
}

