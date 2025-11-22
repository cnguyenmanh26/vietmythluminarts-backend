package com.social.back_java.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import java.util.Date;

@Data
@Embeddable
public class OrderStatusHistory {
    private String status;
    private String note;
    private Long updatedBy; // Storing User ID to avoid complex relationship in embeddable, or could be ManyToOne if Entity
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp = new Date();
}
