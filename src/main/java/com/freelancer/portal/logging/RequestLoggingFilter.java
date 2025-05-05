package com.freelancer.portal.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter for adding correlation IDs to requests for tracing and logging.
 * This filter adds a unique ID to each request and logs request/response details.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String REQUEST_TIME_KEY = "requestStartTime";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request, 
            @NonNull HttpServletResponse response, 
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        // Get or generate correlation ID
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = generateCorrelationId();
        }
        
        // Add correlation ID to MDC for logging
        MDC.put(CORRELATION_ID_KEY, correlationId);
        
        // Add correlation ID to response headers
        response.addHeader(CORRELATION_ID_HEADER, correlationId);
        
        // Record request start time
        request.setAttribute(REQUEST_TIME_KEY, System.currentTimeMillis());
        
        try {
            // Log request details
            logRequest(request, correlationId);
            
            // Continue with the filter chain
            filterChain.doFilter(request, response);
        } finally {
            // Log response details
            logResponse(request, response, correlationId);
            
            // Clean up MDC
            MDC.remove(CORRELATION_ID_KEY);
        }
    }
    
    private void logRequest(HttpServletRequest request, String correlationId) {
        String userAgent = request.getHeader("User-Agent");
        log.info("Request: {} {} [{}] from IP: {}, User-Agent: {}, Correlation-ID: {}",
                request.getMethod(),
                request.getRequestURI(),
                request.getProtocol(),
                request.getRemoteAddr(),
                userAgent != null ? userAgent : "unknown",
                correlationId);
    }
    
    private void logResponse(HttpServletRequest request, HttpServletResponse response, String correlationId) {
        Long startTime = (Long) request.getAttribute(REQUEST_TIME_KEY);
        long duration = System.currentTimeMillis() - startTime;
        
        log.info("Response: {} {} - {} [{}ms] Correlation-ID: {}",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration,
                correlationId);
    }
    
    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}