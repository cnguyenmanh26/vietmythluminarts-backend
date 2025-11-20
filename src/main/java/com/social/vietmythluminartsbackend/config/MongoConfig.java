package com.social.vietmythluminartsbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * MongoDB configuration
 * Enables auditing for @CreatedDate and @LastModifiedDate
 * 
 * IMPORTANT: Auto-index creation is DISABLED to prevent automatic schema changes
 * in existing MongoDB Atlas database. Only data operations (CRUD) will be performed.
 * 
 * Your existing MongoDB Atlas database will NOT be modified:
 * - No new indexes will be created automatically
 * - No collections will be created until first document is inserted
 * - Only data operations (CRUD) will be performed
 * - Schema structure remains unchanged
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig extends AbstractMongoClientConfiguration {

    /**
     * Disable automatic index creation
     * This prevents Spring Data MongoDB from automatically creating indexes
     * based on @Indexed annotations, preserving existing database structure
     * 
     * @return false to disable auto-index creation
     */
    @Override
    protected boolean autoIndexCreation() {
        return false; // Disable auto-index creation
    }

    /**
     * Database name is configured via application.yml connection string
     * No need to override getDatabaseName()
     */
    @Override
    protected String getDatabaseName() {
        return null; // Will use database from connection string in application.yml
    }
}

