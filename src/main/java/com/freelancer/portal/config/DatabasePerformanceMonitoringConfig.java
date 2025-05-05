package com.freelancer.portal.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration for database performance monitoring.
 * Sets up metrics collection and transaction management for query performance monitoring.
 */
@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
@EnableAspectJAutoProxy
public class DatabasePerformanceMonitoringConfig {

    private final Environment env;

    public DatabasePerformanceMonitoringConfig(Environment env) {
        this.env = env;
    }

    /**
     * Configure metrics for timing database operations.
     * @param registry The metrics registry
     * @return TimedAspect for measuring operation duration
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * Configure transaction manager with optimized settings.
     * @param entityManagerFactory The JPA entity manager factory
     * @return Configured transaction manager
     */
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        // Configure transaction timeout for long-running queries
        transactionManager.setDefaultTimeout(30); // 30 seconds timeout
        return transactionManager;
    }

    /**
     * Check if the current environment is a development environment.
     * @return true if in development environment
     */
    private boolean isDevelopment() {
        String[] activeProfiles = env.getActiveProfiles();
        for (String profile : activeProfiles) {
            if ("dev".equals(profile) || "development".equals(profile)) {
                return true;
            }
        }
        return false;
    }
}