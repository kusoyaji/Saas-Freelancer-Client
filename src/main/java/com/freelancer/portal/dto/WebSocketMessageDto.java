package com.freelancer.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for WebSocket messages used in real-time communication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessageDto {
    
    /**
     * Message type, e.g., TIMER_STARTED, TIMER_STOPPED, TIMER_SYNC
     */
    private String type;
    
    /**
     * TimeEntry ID related to this message (if applicable)
     */
    private Long timeEntryId;
    
    /**
     * Additional data to be sent with the message
     */
    private Object data;
}