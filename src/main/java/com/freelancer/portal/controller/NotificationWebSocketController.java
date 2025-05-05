package com.freelancer.portal.controller;

import com.freelancer.portal.dto.NotificationDto;
import com.freelancer.portal.model.Notification;
import com.freelancer.portal.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class NotificationWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;
    
    /**
     * Handle notification acknowledgments from clients
     */
    @MessageMapping("/notifications.acknowledge")
    public void acknowledgeNotification(@Payload Map<String, Object> payload) {
        try {
            Long notificationId = Long.valueOf(payload.get("id").toString());
            String userId = payload.get("userId").toString();
            
            log.info("Received acknowledgment for notification {}, user {}", notificationId, userId);
            
            // Handle notification acknowledgement logic if needed
        } catch (Exception e) {
            log.error("Error processing notification acknowledgment: {}", e.getMessage());
        }
    }
    
    /**
     * This method can be called from other services to send notifications to specific users
     */
    public void sendNotificationToUser(String userId, NotificationDto notification) {
        try {
            Map<String, Object> message = Map.of(
                "type", "NOTIFICATION",
                "data", notification
            );
            
            messagingTemplate.convertAndSendToUser(
                userId, 
                "/queue/notifications", 
                message
            );
            
            log.info("Sent notification to user {}: {}", userId, notification.getTitle());
        } catch (Exception e) {
            log.error("Error sending notification via WebSocket: {}", e.getMessage());
        }
    }
}