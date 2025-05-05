package com.freelancer.portal.mapper;

import com.freelancer.portal.dto.MessageDto;
import com.freelancer.portal.model.Message;

/**
 * Utility class for mapping Message entities to DTOs and vice versa.
 */
public class MessageMapper {

    /**
     * Maps a Message entity to a MessageDto.
     *
     * @param message the message entity
     * @return the message DTO
     */
    public static MessageDto toDto(Message message) {
        if (message == null) {
            return null;
        }
        
        return MessageDto.builder()
                .id(message.getId())
                .content(message.getContent())
                .projectId(message.getProject() != null ? message.getProject().getId() : null)
                .projectName(message.getProject() != null ? message.getProject().getName() : null)
                .conversationId(message.getConversation() != null ? message.getConversation().getId() : null)
                // Enhanced sender information
                .senderId(message.getSender() != null ? message.getSender().getId() : null)
                .senderName(message.getSender() != null ? 
                        (message.getSender().getFirstName() != null ? message.getSender().getFirstName() : "") + " " + 
                        (message.getSender().getLastName() != null ? message.getSender().getLastName() : "").trim() : null)
                .senderProfileImageUrl(message.getSender() != null ? message.getSender().getProfilePictureUrl(): null)
                // Message status fields
                .isRead(message.getIsRead() != null ? message.getIsRead() : false)
                // Attachment fields
                .fileAttachmentUrl(message.getFileAttachmentUrl())
                .fileAttachmentName(extractFileNameFromUrl(message.getFileAttachmentUrl()))

                .createdAt(message.getCreatedAt())
                .sentAt(message.getSentAt())
                .readAt(message.getReadAt())
                .build();
    }
    
    /**
     * Extracts the file name from a file URL.
     *
     * @param url the file URL
     * @return the file name
     */
    private static String extractFileNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex == -1 || lastSlashIndex == url.length() - 1) {
            return url;
        }
        
        return url.substring(lastSlashIndex + 1);
    }
}