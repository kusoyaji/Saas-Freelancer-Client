package com.freelancer.portal.service;

import com.freelancer.portal.dto.ConversationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ConversationService {
    /**
     * Creates a new conversation with the specified participants.
     * 
     * @param participantIds List of user IDs who will participate in the conversation
     * @return The created conversation DTO
     */
    ConversationDto createConversation(List<Long> participantIds);
    
    /**
     * Retrieves all conversations for a specific user.
     * 
     * @param userId The ID of the user
     * @param pageable Pagination information
     * @return Page of conversation DTOs
     */
    Page<ConversationDto> getConversationsForUser(Long userId, Pageable pageable);
    
    /**
     * Retrieves a conversation by its ID.
     * 
     * @param id The conversation ID
     * @return The conversation DTO
     */
    ConversationDto getConversationById(Long id);
    
    /**
     * Adds a user to an existing conversation.
     * 
     * @param conversationId The conversation ID
     * @param userId The user ID to add
     * @return The updated conversation DTO
     */
    ConversationDto addParticipant(Long conversationId, Long userId);
    
    /**
     * Removes a user from an existing conversation.
     * 
     * @param conversationId The conversation ID
     * @param userId The user ID to remove
     * @return The updated conversation DTO
     */
    ConversationDto removeParticipant(Long conversationId, Long userId);
    
    /**
     * Deletes a conversation and all its messages.
     * 
     * @param id The conversation ID
     */
    void deleteConversation(Long id);
}