package com.freelancer.portal.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for logging audit events in the system.
 * This creates structured logs for security-sensitive operations.
 */
public class AuditLogger {
    
    private static final Logger logger = LoggerFactory.getLogger("com.freelancer.portal.audit");
    private static final String KEY_ACTION = "action";
    private static final String KEY_ENTITY_TYPE = "entityType";
    private static final String KEY_ENTITY_ID = "entityId";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_AUTHORITIES = "authorities";
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_DETAILS = "details";
    private static final String KEY_OPERATION = "operation";
    private static final String ANONYMOUS = "anonymous";
    
    // Private constructor to prevent instantiation
    private AuditLogger() {
        // Utility class should not be instantiated
    }
    
    /**
     * Log an audit event with details about the action performed
     *
     * @param action      The action being performed (e.g., "LOGIN", "FILE_UPLOAD")
     * @param entityType  The type of entity being acted upon (e.g., "USER", "PROJECT")
     * @param entityId    The ID of the entity being acted upon
     * @param details     Additional details about the action
     */
    public static void logAuditEvent(String action, String entityType, String entityId, Map<String, Object> details) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put(KEY_ACTION, action);
        auditData.put(KEY_ENTITY_TYPE, entityType);
        auditData.put(KEY_ENTITY_ID, entityId);
        auditData.put(KEY_TIMESTAMP, System.currentTimeMillis());
        
        // Add user information if available
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
                !ANONYMOUS.equals(authentication.getPrincipal().toString())) {
            auditData.put(KEY_USERNAME, authentication.getName());
            auditData.put(KEY_AUTHORITIES, authentication.getAuthorities());
        } else {
            auditData.put(KEY_USERNAME, ANONYMOUS);
        }
        
        // Add any additional details
        if (details != null) {
            auditData.putAll(details);
        }
        
        logger.info("Audit event: {}", auditData);
    }
    
    /**
     * Log a security-related audit event
     */
    public static void logSecurityEvent(String action, String username, boolean success, String details) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put(KEY_ACTION, action);
        auditData.put(KEY_USERNAME, username);
        auditData.put(KEY_SUCCESS, success);
        auditData.put(KEY_DETAILS, details);
        
        logger.info("Security event: {}", auditData);
    }
    
    /**
     * Log a data access event
     */
    public static void logDataAccess(String entityType, String entityId, String operation) {
        Map<String, Object> details = new HashMap<>();
        details.put(KEY_OPERATION, operation);
        
        logAuditEvent("DATA_ACCESS", entityType, entityId, details);
    }
    
    /**
     * Log an administrative action
     */
    public static void logAdminAction(String action, String targetType, String targetId, String details) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put(KEY_DETAILS, details);
        
        logAuditEvent("ADMIN_" + action, targetType, targetId, auditData);
    }
}