package com.freelancer.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Data Transfer Object for conversation information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDto {
    
    private Long id;
    private String title;
    private Long projectId;
    private String projectName;
    private Set<ParticipantDto> participants;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long unreadMessagesCount;
    private MessageDto lastMessage;
    
    /**
     * Simplified DTO for conversation participants
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantDto {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String profileImageUrl;
    }
}