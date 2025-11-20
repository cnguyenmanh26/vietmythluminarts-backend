package com.social.vietmythluminartsbackend.model.embedded;

import com.social.vietmythluminartsbackend.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;

/**
 * Embedded document for cart items
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @DBRef
    private Product product;

    @Builder.Default
    private Integer quantity = 1;

    private Double price;

    /**
     * Snapshot of product details at time of adding to cart
     */
    private ProductSnapshot productSnapshot;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSnapshot {
        private String name;
        private String image;
        private String category;
    }
}

