package com.freelancer.portal.service;

import com.freelancer.portal.dto.MessageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface MessageService {
    Page<MessageDto> getMessagesByProject(Long projectId, Pageable pageable);
    MessageDto getMessageById(Long id);
    MessageDto createMessage(MessageDto messageDto);
    void markMessageAsRead(Long id);
    void deleteMessage(Long id);
    long countUnreadMessages(Long projectId);
    
    /**
     * Sends a message to a conversation with optional file attachment.
     * 
     * @param conversationId The conversation ID
     * @param content The message content
     * @param file Optional file attachment
     * @return The sent message DTO
     */
    MessageDto sendMessage(Long conversationId, String content, MultipartFile file) throws IOException;
    
    /**
     * Retrieves messages from a specific conversation.
     * 
     * @param conversationId The conversation ID
     * @param pageable Pagination information
     * @return Page of message DTOs
     */
    Page<MessageDto> getMessagesByConversation(Long conversationId, Pageable pageable);
    
    /**
     * Counts unread messages for a conversation.
     * 
     * @param conversationId The conversation ID
     * @return The count of unread messages
     */
    long countUnreadConversationMessages(Long conversationId);
}