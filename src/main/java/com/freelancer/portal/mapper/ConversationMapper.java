package com.freelancer.portal.mapper;

import com.freelancer.portal.dto.ConversationDto;
import com.freelancer.portal.dto.MessageDto;
import com.freelancer.portal.model.Conversation;
import com.freelancer.portal.model.Message;
import com.freelancer.portal.model.User;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for mapping Conversation entities to DTOs and vice versa.
 */
public class ConversationMapper {

    /**
     * Maps a Conversation entity to a ConversationDto.
     *
     * @param conversation the conversation entity
     * @param currentUserId the ID of the current user (to calculate unread messages)
     * @return the conversation DTO
     */
    public static ConversationDto toDto(Conversation conversation, Long currentUserId) {
        if (conversation == null) {
            return null;
        }
        
        // Map participants
        Set<ConversationDto.ParticipantDto> participantDtos = conversation.getParticipants().stream()
                .map(user -> ConversationDto.ParticipantDto.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .profileImageUrl(user.getProfilePictureUrl())
                        .build())
                .collect(Collectors.toSet());
        
        // Find last message
        MessageDto lastMessageDto = null;
        if (conversation.getMessages() != null && !conversation.getMessages().isEmpty()) {
            Optional<Message> lastMessage = conversation.getMessages().stream()
                    .max(Comparator.comparing(Message::getSentAt));
            
            if (lastMessage.isPresent()) {
                lastMessageDto = MessageMapper.toDto(lastMessage.get());
            }
        }
        
        // Calculate unread messages count for the current user
        long unreadCount = 0;
        if (currentUserId != null && conversation.getMessages() != null) {
            unreadCount = conversation.getMessages().stream()
                    .filter(message -> !message.getIsRead() && 
                                      !message.getSender().getId().equals(currentUserId))
                    .count();
        }
        
        return ConversationDto.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .projectId(conversation.getProject() != null ? conversation.getProject().getId() : null)
                .projectName(conversation.getProject() != null ? conversation.getProject().getName() : null)
                .participants(participantDtos)
                .lastMessageAt(conversation.getLastMessageAt())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .unreadMessagesCount(unreadCount)
                .lastMessage(lastMessageDto)
                .build();
    }
}