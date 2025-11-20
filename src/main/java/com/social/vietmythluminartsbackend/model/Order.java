package com.social.vietmythluminartsbackend.model;

import com.social.vietmythluminartsbackend.model.embedded.Address;
import com.social.vietmythluminartsbackend.model.embedded.OrderItem;
import com.social.vietmythluminartsbackend.model.embedded.StatusHistory;
import com.social.vietmythluminartsbackend.model.enums.OrderStatus;
import com.social.vietmythluminartsbackend.model.enums.PaymentMethod;
import com.social.vietmythluminartsbackend.model.enums.PaymentStatus;
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
 * Order entity
 * Migrated from Node.js Order model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {

    @Id
    private String id;

    @DBRef
    private User user;

    @Indexed(unique = true)
    private String orderNumber;

    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    // Pricing
    private Double subtotal;
    @Builder.Default
    private Double tax = 0.0; // 10%
    @Builder.Default
    private Double discount = 0.0;
    private Double total;

    // Shipping Address (using Address embedded document)
    private Address shippingAddress;

    // Payment
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.COD;
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.Pending;
    private LocalDateTime paymentDate;
    private String transactionId;

    // Order Status
    @Builder.Default
    private OrderStatus status = OrderStatus.Pending;

    // Status History
    @Builder.Default
    private List<StatusHistory> statusHistory = new ArrayList<>();

    // Additional Info
    private String notes;
    private String cancelReason;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ==================== Business Logic Methods ====================

    /**
     * Check if order can be cancelled
     */
    public boolean canBeCancelled() {
        return status == OrderStatus.Pending || status == OrderStatus.Confirmed;
    }

    /**
     * Add status history entry
     */
    public void addStatusHistory(String status, String note, User updatedBy) {
        StatusHistory history = StatusHistory.builder()
                .status(status)
                .note(note)
                .updatedBy(updatedBy)
                .timestamp(LocalDateTime.now())
                .build();
        this.statusHistory.add(history);
    }
}

