package com.freelancer.portal.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Configuration for automated alerts based on metrics and application health.
 * This class monitors critical metrics and sends alerts when thresholds are breached.
 */
@Configuration
@EnableScheduling
public class AlertingConfig {

    private static final Logger logger = LoggerFactory.getLogger(AlertingConfig.class);
    private static final String HTTP_SERVER_REQUESTS = "http.server.requests";
    private static final String JVM_MEMORY_USED = "jvm.memory.used";
    private static final String JVM_MEMORY_MAX = "jvm.memory.max";
    private static final String AREA_HEAP = "heap";
    private static final String TAG_AREA = "area";
    private static final String TAG_STATUS = "status";
    private static final String STATUS_5XX = "5xx";
    
    private final MeterRegistry meterRegistry;
    private final RestTemplate restTemplate;
    
    @Value("${alerts.slack.webhook:}")
    private String slackWebhookUrl;
    
    @Value("${alerts.email.to:}")
    private String alertEmailTo;
    
    @Value("${alerts.enabled:false}")
    private boolean alertsEnabled;
    
    private AtomicInteger errorCounter = new AtomicInteger(0);
    private AtomicInteger warningCounter = new AtomicInteger(0);
    
    public AlertingConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.restTemplate = new RestTemplate();
    }
    
    @PostConstruct
    public void init() {
        // Register gauges for error and warning counts
        Gauge.builder("alerts.errors", errorCounter, AtomicInteger::get)
                .description("Number of error alerts triggered")
                .register(meterRegistry);
        
        Gauge.builder("alerts.warnings", warningCounter, AtomicInteger::get)
                .description("Number of warning alerts triggered")
                .register(meterRegistry);
    }
    
    /**
     * Check for high error rates in the application
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    public void checkErrorRates() {
        if (!alertsEnabled) return;
        
        try {
            double errorCount = getErrorCount();
            double totalCount = getTotalRequestCount();
            
            // Only proceed if we have requests
            if (totalCount > 0) {
                // Calculate error rate
                double errorRate = errorCount / totalCount;
                
                // Alert if error rate is above threshold
                if (errorRate > 0.05) { // 5% error rate threshold
                    String message = String.format("High error rate alert: %.2f%% of requests are failing", errorRate * 100);
                    sendAlert("ERROR", message);
                    errorCounter.incrementAndGet();
                }
            }
        } catch (Exception e) {
            logger.error("Error checking error rates", e);
        }
    }
    
    /**
     * Get count of error responses (5xx status codes)
     */
    private double getErrorCount() {
        try {
            return meterRegistry.get(HTTP_SERVER_REQUESTS)
                .tag(TAG_STATUS, STATUS_5XX)
                .counter()
                .count();
        } catch (Exception e) {
            logger.debug("No 5xx errors recorded yet: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Get total request count
     */
    private double getTotalRequestCount() {
        try {
            return meterRegistry.get(HTTP_SERVER_REQUESTS)
                .counter()
                .count();
        } catch (Exception e) {
            logger.debug("No requests recorded yet: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Check for slow API response times
     */
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void checkResponseTimes() {
        if (!alertsEnabled) return;
        
        try {
            Double p95ResponseTimeMs = getP95ResponseTime();
            
            if (p95ResponseTimeMs != null && p95ResponseTimeMs > 500) {
                String message = String.format("Slow API response alert: 95th percentile response time is %.2fms", p95ResponseTimeMs);
                sendAlert("WARNING", message);
                warningCounter.incrementAndGet();
            }
        } catch (Exception e) {
            logger.error("Error checking response times", e);
        }
    }
    
    /**
     * Get 95th percentile response time in milliseconds
     */
    private Double getP95ResponseTime() {
        try {
            Timer timer = meterRegistry.find(HTTP_SERVER_REQUESTS).timer();
            if (timer != null) {
                // Alternative approach to access timer metrics
                double p95Value = 0.0;
                try {
                    // Try to get 95th percentile from distribution summary
                    p95Value = timer.percentile(0.95, TimeUnit.MILLISECONDS);
                    if (p95Value > 0) {
                        return p95Value;
                    }
                    
                    // If percentile returns 0, try alternative method
                    p95Value = timer.max(TimeUnit.MILLISECONDS) * 0.95; // Approximation
                    return p95Value;
                } catch (Exception e) {
                    logger.debug("Could not get percentile directly: {}", e.getMessage());
                    // Fallback to max value as a rough approximation 
                    return timer.max(TimeUnit.MILLISECONDS);
                }
            }
            return null;
        } catch (Exception e) {
            logger.debug("Could not get response time metrics: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Check for high memory usage
     */
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void checkMemoryUsage() {
        if (!alertsEnabled) return;
        
        try {
            MemoryUsage memoryUsage = getHeapMemoryUsage();
            
            if (memoryUsage != null && memoryUsage.maxMemory > 0) {
                double usedHeapPct = memoryUsage.usedMemory / memoryUsage.maxMemory;
                
                // Alert if memory usage is above threshold
                if (usedHeapPct > 0.85) { // 85% threshold
                    String message = String.format("High memory usage alert: %.2f%% of heap memory is used", usedHeapPct * 100);
                    sendAlert("WARNING", message);
                    warningCounter.incrementAndGet();
                }
            }
        } catch (Exception e) {
            logger.error("Error checking memory usage", e);
        }
    }
    
    /**
     * Get heap memory usage statistics
     */
    private MemoryUsage getHeapMemoryUsage() {
        try {
            double usedHeap = meterRegistry.get(JVM_MEMORY_USED)
                    .tag(TAG_AREA, AREA_HEAP)
                    .gauge()
                    .value();
            
            double maxHeap = meterRegistry.get(JVM_MEMORY_MAX)
                    .tag(TAG_AREA, AREA_HEAP)
                    .gauge()
                    .value();
            
            return new MemoryUsage(usedHeap, maxHeap);
        } catch (Exception e) {
            logger.debug("Could not get memory metrics: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Helper class to hold memory usage data
     */
    private static class MemoryUsage {
        final double usedMemory;
        final double maxMemory;
        
        public MemoryUsage(double usedMemory, double maxMemory) {
            this.usedMemory = usedMemory;
            this.maxMemory = maxMemory;
        }
    }
    
    /**
     * Send an alert via configured channels (Slack, email, etc.)
     */
    private void sendAlert(String level, String message) {
        logger.warn("ALERT [{}]: {}", level, message);
        
        // Log the alert
        if ("ERROR".equals(level)) {
            logger.error("System alert: {}", message);
        } else {
            logger.warn("System alert: {}", message);
        }
        
        // Send Slack notification if configured
        if (slackWebhookUrl != null && !slackWebhookUrl.isEmpty()) {
            try {
                Map<String, Object> slackPayload = new HashMap<>();
                slackPayload.put("text", level + " ALERT: " + message);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(slackPayload, headers);
                
                ResponseEntity<String> response = restTemplate.postForEntity(slackWebhookUrl, entity, String.class);
                
                if (!response.getStatusCode().is2xxSuccessful()) {
                    logger.error("Failed to send Slack alert: {}", response.getStatusCode());
                }
            } catch (Exception e) {
                logger.error("Error sending Slack alert", e);
            }
        }
        
        // Add email alerting implementation here
    }
}