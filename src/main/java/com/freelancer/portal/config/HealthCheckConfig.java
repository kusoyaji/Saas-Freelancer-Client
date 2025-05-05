package com.freelancer.portal.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for application health checks.
 * This class defines custom health indicators for critical services.
 */
@Configuration
public class HealthCheckConfig {

    private static final String DATABASE = "database";
    private static final String STATUS = "status";
    private static final String ERROR = "error";
    private static final String DATABASE_TYPE = "MySQL";
    private static final String PATH = "path";
    private static final String FREE_SPACE = "free_space";
    private static final String TOTAL_SPACE = "total_space";
    private static final String PERCENT_FREE = "percent_free";
    private static final String THRESHOLD = "threshold";

    /**
     * Custom database health indicator to check database connectivity
     */
    @Bean
    public HealthIndicator databaseHealthIndicator(JdbcTemplate jdbcTemplate) {
        return () -> {
            try {
                // Execute a simple query to check database connectivity
                Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                
                if (result != null && result == 1) {
                    Map<String, Object> details = new HashMap<>();
                    details.put(DATABASE, DATABASE_TYPE);
                    details.put(STATUS, "UP");
                    
                    return Health.up()
                            .withDetails(details)
                            .build();
                } else {
                    return Health.down()
                            .withDetail(DATABASE, DATABASE_TYPE)
                            .withDetail(STATUS, "DOWN")
                            .withDetail(ERROR, "Invalid response from database")
                            .build();
                }
            } catch (Exception e) {
                return Health.down()
                        .withDetail(DATABASE, DATABASE_TYPE)
                        .withDetail(STATUS, "DOWN")
                        .withDetail(ERROR, e.getMessage())
                        .build();
            }
        };
    }
    
    /**
     * Custom file storage health indicator
     */
    @Bean
    public HealthIndicator fileStorageHealthIndicator() {
        return () -> {
            File uploadDir = new File("uploads");
            boolean exists = uploadDir.exists();
            boolean canWrite = uploadDir.canWrite();
            
            if (exists && canWrite) {
                Map<String, Object> details = new HashMap<>();
                details.put(PATH, uploadDir.getAbsolutePath());
                details.put(FREE_SPACE, uploadDir.getFreeSpace());
                details.put(TOTAL_SPACE, uploadDir.getTotalSpace());
                
                return Health.up()
                        .withDetails(details)
                        .build();
            } else {
                String error = exists ? "Directory not writable" : "Directory does not exist";
                return Health.down()
                        .withDetail(PATH, uploadDir.getAbsolutePath())
                        .withDetail(ERROR, error)
                        .build();
            }
        };
    }
    
    /**
     * Custom disk space health indicator
     */
    @Bean
    public HealthIndicator diskSpaceHealthIndicator() {
        return () -> {
            File path = new File(".");
            long freeSpace = path.getFreeSpace();
            long totalSpace = path.getTotalSpace();
            long threshold = 10L * 1024L * 1024L * 1024L; // 10GB
            
            if (freeSpace < threshold) {
                return Health.down()
                        .withDetail(PATH, path.getAbsolutePath())
                        .withDetail(FREE_SPACE, freeSpace)
                        .withDetail(THRESHOLD, threshold)
                        .build();
            } else {
                double percentFree = (double) freeSpace / totalSpace * 100;
                return Health.up()
                        .withDetail(PATH, path.getAbsolutePath())
                        .withDetail(FREE_SPACE, freeSpace)
                        .withDetail(TOTAL_SPACE, totalSpace)
                        .withDetail(PERCENT_FREE, String.format("%.2f%%", percentFree))
                        .build();
            }
        };
    }
}