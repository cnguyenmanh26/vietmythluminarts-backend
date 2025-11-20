package com.social.vietmythluminartsbackend.model.embedded;

import com.social.vietmythluminartsbackend.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;

/**
 * Embedded document for order items
 * Contains snapshot of product at time of order
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @DBRef
    private Product product;

    private String name;
    private String image;
    private String category;
    private Double price;
    private Integer quantity;
    private Double subtotal;
}

