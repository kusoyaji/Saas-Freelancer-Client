package com.freelancer.portal.controller;

import com.freelancer.portal.dto.NotificationDto;
import com.freelancer.portal.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for handling WebSocket messages.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class
WebSocketMessageController {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    /**
     * Handle connection test message from clients.
     */
    @MessageMapping("/connect")
    public void handleConnect(SimpMessageHeaderAccessor headerAccessor) {
        Principal user = headerAccessor.getUser();
        if (user != null) {
            String username = user.getName();
            log.info("Received connection test from: {}", username);
            
            // Send connection acknowledgment back to the user
            Map<String, Object> response = new HashMap<>();
            response.put("type", "CONNECT_ACK");
            response.put("message", "Connection established successfully");
            
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/notifications",
                response
            );
        } else {
            log.warn("Received anonymous connection test message");
        }
    }

    /**
     * Debug endpoint to test if WebSocket routes are accessible.
     */
    @GetMapping("/api/v1/ws/test")
    @ResponseBody
    public Map<String, String> testWsEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "WebSocket endpoint is accessible");
        return response;
    }
    
    /**
     * Send a notification to a specific user.
     */
    public void sendNotificationToUser(String userId, NotificationDto notification) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "NOTIFICATION");
            message.put("data", notification);
            
            messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/notifications",
                message
            );
            
            log.info("Sent notification to user {}: {}", userId, notification.getTitle());
        } catch (Exception e) {
            log.error("Error sending notification via WebSocket: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Send a notification to all connected users.
     */
    public void broadcastNotification(NotificationDto notification) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "NOTIFICATION");
            message.put("data", notification);
            
            messagingTemplate.convertAndSend("/topic/notifications", message);
            log.info("Broadcast notification: {}", notification.getTitle());
        } catch (Exception e) {
            log.error("Error broadcasting notification: {}", e.getMessage(), e);
        }
    }
}