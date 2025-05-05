package com.freelancer.portal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;

import java.util.Map;

/**
 * Configuration class for optimizing JOIN operations and query execution.
 * Provides global query hints and configuration for Hibernate.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.freelancer.portal.repository")
public class QueryHintConfiguration {

    /**
     * Configure Hibernate properties to optimize query performance
     * particularly for JOIN operations.
     * 
     * @return HibernatePropertiesCustomizer to customize Hibernate properties
     */
    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return hibernateProperties -> {
            // Configure batch fetching to reduce the number of database queries
            hibernateProperties.put("hibernate.default_batch_fetch_size", 25);
            
            // Enable join fetching for collections to prevent N+1 queries
            hibernateProperties.put("hibernate.collection_fetch_mode", "join");
            
            // Optimize SQL query generation
            hibernateProperties.put("hibernate.query.fail_on_pagination_over_collection_fetch", true);
            hibernateProperties.put("hibernate.query.in_clause_parameter_padding", true);
            
            // Enable query plan caching for better performance
            hibernateProperties.put("hibernate.query.plan_cache_max_size", 2048);
            hibernateProperties.put("hibernate.query.plan_parameter_metadata_max_size", 128);
            
            // Optimize connection and statement handling
            hibernateProperties.put("hibernate.connection.provider_disables_autocommit", true);
            hibernateProperties.put("hibernate.jdbc.batch_versioned_data", true);
            
            // Set fetch size for optimized data retrieval
            hibernateProperties.put("hibernate.jdbc.fetch_size", 50);
        };
    }
}