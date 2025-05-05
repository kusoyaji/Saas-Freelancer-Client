package com.freelancer.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for message information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    
    private Long id;
    private String content;
    private Long projectId;
    private String projectName;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String senderProfileImageUrl;
    private Boolean isRead;
    private String fileAttachmentUrl;
    private String fileAttachmentName;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
}