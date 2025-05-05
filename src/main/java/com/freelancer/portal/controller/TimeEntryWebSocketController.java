package com.freelancer.portal.controller;

import com.freelancer.portal.dto.TimeEntryDto;
import com.freelancer.portal.dto.WebSocketMessageDto;
import com.freelancer.portal.security.SecurityUtils;
import com.freelancer.portal.service.TimeEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * WebSocket controller for real-time time entry operations
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class TimeEntryWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final TimeEntryService timeEntryService;
    private final SecurityUtils securityUtils;

    /**
     * Process timer sync messages from clients
     */
    @MessageMapping("/timer/sync")
    public void processTimerSync(@Payload WebSocketMessageDto message) {
        try {
            String username = securityUtils.getCurrentUser().getUsername();
            log.debug("Received timer sync message from {}: {}", username, message);
            
            // Forward the message to the user's private channel
            String userDestination = "/queue/user/" + username + "/timer";
            messagingTemplate.convertAndSend(userDestination, message);
            
            // If this is a stop message, we might want to update the database
            if ("TIMER_STOPPED".equals(message.getType()) && message.getTimeEntryId() != null) {
                // Update the time entry in database (optional, as REST API already handles this)
                log.debug("Timer stopped for time entry ID: {}", message.getTimeEntryId());
            }
        } catch (Exception e) {
            log.error("Error processing timer sync message", e);
        }
    }

    /**
     * Send a notification to a user about timer events
     */
    public void sendTimerNotification(String username, String type, Long timeEntryId, Object data) {
        WebSocketMessageDto message = new WebSocketMessageDto();
        message.setType(type);
        message.setTimeEntryId(timeEntryId);
        message.setData(data);
        
        String userDestination = "/queue/user/" + username + "/timer";
        messagingTemplate.convertAndSend(userDestination, message);
        log.debug("Sent timer notification to {}: {}", username, message);
    }
}