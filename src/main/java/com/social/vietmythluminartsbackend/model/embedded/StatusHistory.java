package com.social.vietmythluminartsbackend.model.embedded;

import com.social.vietmythluminartsbackend.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;

/**
 * Embedded document for order status history
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusHistory {

    private String status;
    private String note;

    @DBRef
    private User updatedBy;

    private LocalDateTime timestamp;
}

