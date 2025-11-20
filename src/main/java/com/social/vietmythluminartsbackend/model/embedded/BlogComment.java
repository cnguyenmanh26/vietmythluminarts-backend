package com.social.vietmythluminartsbackend.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;

/**
 * Embedded document for blog comments
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogComment {

    @DBRef
    private com.social.vietmythluminartsbackend.model.User user;

    private String content;

    private LocalDateTime createdAt;
}

