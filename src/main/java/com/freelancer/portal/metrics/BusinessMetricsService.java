package com.freelancer.portal.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service for tracking business-specific metrics in the application.
 * This service provides methods to record various business events and their timing information.
 */
@Service
public class BusinessMetricsService {
    
    private final MeterRegistry meterRegistry;
    
    public BusinessMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    /**
     * Record a project creation event
     * 
     * @param projectType The type of project created
     */
    public void recordProjectCreation(String projectType) {
        Counter.builder("freelancer.projects.created")
                .tag("type", projectType)
                .description("Number of projects created")
                .register(meterRegistry)
                .increment();
    }
    
    /**
     * Record a project completion event
     * 
     * @param projectType The type of project completed
     */
    public void recordProjectCompletion(String projectType) {
        Counter.builder("freelancer.projects.completed")
                .tag("type", projectType)
                .description("Number of projects completed")
                .register(meterRegistry)
                .increment();
    }
    
    /**
     * Record an invoice payment
     * 
     * @param paymentMethod The payment method used
     * @param amount The amount paid
     */
    public void recordPayment(String paymentMethod, double amount) {
        Counter.builder("freelancer.payments.total")
                .tag("method", paymentMethod)
                .description("Total payments")
                .register(meterRegistry)
                .increment();
        
        Counter.builder("freelancer.payments.amount")
                .tag("method", paymentMethod)
                .description("Payment amounts")
                .register(meterRegistry)
                .increment(amount);
    }
    
    /**
     * Record a user registration event
     * 
     * @param userType The type of user registered (freelancer, client, etc.)
     */
    public void recordUserRegistration(String userType) {
        Counter.builder("freelancer.users.registered")
                .tag("type", userType)
                .description("Number of users registered")
                .register(meterRegistry)
                .increment();
    }
    
    /**
     * Record user login event
     * 
     * @param success Whether the login was successful
     */
    public void recordUserLogin(boolean success) {
        List<Tag> tags = Arrays.asList(Tag.of("success", String.valueOf(success)));
        
        Counter.builder("freelancer.users.logins")
                .tags(tags)
                .description("Number of user login attempts")
                .register(meterRegistry)
                .increment();
    }
    
    /**
     * Record API call timing
     * 
     * @param endpoint The API endpoint called
     * @param statusCode The HTTP status code returned
     * @param durationMs The duration of the call in milliseconds
     */
    public void recordApiCallTiming(String endpoint, int statusCode, long durationMs) {
        Timer timer = Timer.builder("freelancer.api.request.duration")
                .tag("endpoint", endpoint)
                .tag("status", String.valueOf(statusCode))
                .description("API call duration")
                .register(meterRegistry);
        
        timer.record(durationMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Record file upload
     * 
     * @param fileType The type of file uploaded
     * @param sizeBytes The size of the uploaded file in bytes
     */
    public void recordFileUpload(String fileType, long sizeBytes) {
        Counter.builder("freelancer.files.uploaded")
                .tag("type", fileType)
                .description("Number of files uploaded")
                .register(meterRegistry)
                .increment();
        
        Counter.builder("freelancer.files.size")
                .tag("type", fileType)
                .description("Total size of uploaded files")
                .register(meterRegistry)
                .increment(sizeBytes);
    }
    
    /**
     * Record active user sessions
     * 
     * @param count The number of active sessions
     */
    public void recordActiveSessions(long count) {
        meterRegistry.gauge("freelancer.users.active_sessions", count);
    }
}