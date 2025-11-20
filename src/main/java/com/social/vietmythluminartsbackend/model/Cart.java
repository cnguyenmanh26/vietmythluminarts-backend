package com.social.vietmythluminartsbackend.model;

import com.social.vietmythluminartsbackend.model.embedded.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Shopping cart entity
 * One cart per user
 * Migrated from Node.js Cart model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "carts")
public class Cart {

    @Id
    private String id;

    @DBRef
    @Indexed(unique = true)
    private User user;

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @Builder.Default
    private Integer totalItems = 0;

    @Builder.Default
    private Double totalPrice = 0.0;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ==================== Business Logic Methods ====================

    /**
     * Calculate totals
     * Called before saving
     */
    public void calculateTotals() {
        this.totalItems = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        this.totalPrice = items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    /**
     * Check if cart is empty
     */
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    /**
     * Clear all items
     */
    public void clear() {
        this.items.clear();
        this.totalItems = 0;
        this.totalPrice = 0.0;
    }
}

