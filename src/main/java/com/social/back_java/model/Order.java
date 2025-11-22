package com.social.back_java.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false)
    private Double subtotal;

    @Column(columnDefinition = "DOUBLE DEFAULT 0")
    private Double tax;

    @Column(columnDefinition = "DOUBLE DEFAULT 0")
    private Double discount;

    @Column(nullable = false)
    private Double total;

    @Embedded
    private ShippingAddress shippingAddress;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(columnDefinition = "VARCHAR(255) DEFAULT 'Pending'")
    private String paymentStatus = "Pending";

    @Temporal(TemporalType.TIMESTAMP)
    private Date paymentDate;

    private String transactionId;

    @Column(nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'Pending'")
    private String status = "Pending";

    @ElementCollection
    @CollectionTable(name = "order_status_history", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    @Column(length = 500)
    private String notes;

    private String cancelReason;

    @Temporal(TemporalType.TIMESTAMP)
    private Date deliveredAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date cancelledAt;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}
