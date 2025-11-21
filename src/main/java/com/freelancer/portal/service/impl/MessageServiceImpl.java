package com.freelancer.portal.service.impl;

import com.freelancer.portal.dto.FileResponseDto;
import com.freelancer.portal.dto.MessageDto;
import com.freelancer.portal.mapper.MessageMapper;
import com.freelancer.portal.model.*;
import com.freelancer.portal.repository.ConversationRepository;
import com.freelancer.portal.repository.MessageRepository;
import com.freelancer.portal.repository.ProjectRepository;
import com.freelancer.portal.repository.UserRepository;
import com.freelancer.portal.service.FileService;
import com.freelancer.portal.service.MessageService;
import com.freelancer.portal.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final FileService fileService;
    private final NotificationService notificationService;

    @Override
    public Page<MessageDto> getMessagesByProject(Long projectId, Pageable pageable) {
        User currentUser = getCurrentUser();
        Project project = getProjectForUser(projectId, currentUser);
        
        return messageRepository.findByProject(project, pageable)
                .map(MessageMapper::toDto);
    }

    @Override
    public MessageDto getMessageById(Long id) {
        User currentUser = getCurrentUser();
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message not found with id: " + id));

        // Check if the message is part of a project or conversation that the current user has access to
        if (message.getProject() != null) {
            // Verify the message belongs to a project accessible by the current user
            Project project = message.getProject();
            if (!isUserAuthorizedForProject(currentUser, project)) {
                throw new EntityNotFoundException("Message not found with id: " + id);
            }
        } else if (message.getConversation() != null) {
            // Verify the message belongs to a conversation accessible by the current user
            Conversation conversation = message.getConversation();
            if (!conversation.getParticipants().contains(currentUser)) {
                throw new EntityNotFoundException("Message not found with id: " + id);
            }
        } else {
            throw new EntityNotFoundException("Message not found with id: " + id);
        }

        return MessageMapper.toDto(message);
    }

    @Override
    @Transactional
    public MessageDto createMessage(MessageDto messageDto) {
        User currentUser = getCurrentUser();
        Project project = getProjectForUser(messageDto.getProjectId(), currentUser);
        
        Message message = Message.builder()
                .content(messageDto.getContent())
                .project(project)
                .sender(currentUser)
                .isRead(false)
                .build();
        
        // If there's a conversation associated with the project, link the message to it
        if (messageDto.getConversationId() != null) {
            Conversation conversation = conversationRepository.findById(messageDto.getConversationId())
                    .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));
            message.setConversation(conversation);
        } else {
            // For backward compatibility, get or create a default conversation for the project
            // This might need to be implemented based on your application's business logic
        }
        
        Message savedMessage = messageRepository.save(message);
        
        // Create notification for the message
        notificationService.createMessageNotification(savedMessage);
        
        return MessageMapper.toDto(savedMessage);
    }

    @Override
    @Transactional
    public void markMessageAsRead(Long id) {
        User currentUser = getCurrentUser();
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message not found with id: " + id));

        if (message.getProject() != null) {
            // Verify the message belongs to a project accessible by the current user
            Project project = message.getProject();
            if (!isUserAuthorizedForProject(currentUser, project)) {
                throw new EntityNotFoundException("Message not found with id: " + id);
            }
        } else if (message.getConversation() != null) {
            // Verify the message belongs to a conversation accessible by the current user
            Conversation conversation = message.getConversation();
            if (!conversation.getParticipants().contains(currentUser)) {
                throw new EntityNotFoundException("Message not found with id: " + id);
            }
        } else {
            throw new EntityNotFoundException("Message not found with id: " + id);
        }
        
        // Only mark as read if the current user is not the sender
        if (!message.getSender().getId().equals(currentUser.getId())) {
            message.markAsRead(); // Using the correct method provided in the Message class
            messageRepository.save(message);
        }
    }

    @Override
    @Transactional
    public void deleteMessage(Long id) {
        User currentUser = getCurrentUser();
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message not found with id: " + id));
        
        boolean authorized = false;
        
        if (message.getProject() != null) {
            // Verify the message belongs to a project accessible by the current user
            Project project = message.getProject();
            if (!isUserAuthorizedForProject(currentUser, project)) {
                throw new EntityNotFoundException("Message not found with id: " + id);
            }
            
            // Only allow deletion if the current user is the sender or the freelancer of the project
            authorized = message.getSender().getId().equals(currentUser.getId()) || 
                         project.getFreelancer().getId().equals(currentUser.getId());
        } else if (message.getConversation() != null) {
            // Verify the message belongs to a conversation accessible by the current user
            Conversation conversation = message.getConversation();
            if (!conversation.getParticipants().contains(currentUser)) {
                throw new EntityNotFoundException("Message not found with id: " + id);
            }
            
            // Only allow deletion if the current user is the sender
            authorized = message.getSender().getId().equals(currentUser.getId());
        } else {
            throw new EntityNotFoundException("Message not found with id: " + id);
        }
        
        if (authorized) {
            messageRepository.delete(message);
        } else {
            throw new IllegalStateException("You are not authorized to delete this message");
        }
    }

    @Override
    public long countUnreadMessages(Long projectId) {
        User currentUser = getCurrentUser();
        Project project = getProjectForUser(projectId, currentUser);
        
        return messageRepository.countByProjectAndIsReadFalseAndSenderNot(project, currentUser);
    }
    
    @Override
    @Transactional
    public MessageDto sendMessage(Long conversationId, String content, MultipartFile file) throws IOException {
        User currentUser = getCurrentUser();
        
        Conversation conversation = conversationRepository.findByIdAndParticipantsContains(conversationId, currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found with id: " + conversationId));
        
        // Create message without attachment first
        Message message = Message.builder()
                .content(content)
                .conversation(conversation)
                .sender(currentUser)
                .isRead(false)
                .build();
        
        // If the conversation is related to a project, link the message to it as well
        if (conversation.getProject() != null) {
            message.setProject(conversation.getProject());
        }
        
        Message savedMessage = messageRepository.save(message);
        
        // Handle file attachment if provided
        if (file != null && !file.isEmpty()) {
            try {
                // For messaging, we'll use a simpler direct file storage approach
                // This avoids the need for a project ID
                String entityType = "message";
                Long entityId = savedMessage.getId();
                
                // Use the FileMetadata-based approach which is more robust
                FileMetadata fileMetadata = fileService.upload(entityType, entityId, file);
                
                // Set the file attachment URL on the message
                savedMessage.setFileAttachmentUrl(fileMetadata.getUrl());

                savedMessage = messageRepository.save(savedMessage);
            } catch (Exception ex) {
                // Log the error but don't fail the message sending
                ex.printStackTrace();
                // The message will be saved without the attachment
            }
        }
        
        // Update conversation last message timestamp
        conversation.setLastMessageAt(savedMessage.getSentAt());
        conversationRepository.save(conversation);
        
        // Create notification for the message
        notificationService.createMessageNotification(savedMessage);
        
        return MessageMapper.toDto(savedMessage);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<MessageDto> getMessagesByConversation(Long conversationId, Pageable pageable) {
        User currentUser = getCurrentUser();
        
        Conversation conversation = conversationRepository.findByIdAndParticipantsContains(conversationId, currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found with id: " + conversationId));
        
        return messageRepository.findByConversation(conversation, pageable)
                .map(MessageMapper::toDto);
    }
    
    @Override
    public long countUnreadConversationMessages(Long conversationId) {
        User currentUser = getCurrentUser();
        
        Conversation conversation = conversationRepository.findByIdAndParticipantsContains(conversationId, currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found with id: " + conversationId));
        
        return messageRepository.countByConversationAndIsReadFalseAndSenderNot(conversation, currentUser);
    }
    
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
    
    private Project getProjectForUser(Long projectId, User user) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));
        
        if (!isUserAuthorizedForProject(user, project)) {
            throw new EntityNotFoundException("Project not found with id: " + projectId);
        }
        
        return project;
    }
    
    private boolean isUserAuthorizedForProject(User user, Project project) {
        // A user is authorized for a project if they are the freelancer or the client
        return project.getFreelancer().getId().equals(user.getId()) || 
               (project.getClient() != null && project.getClient().getUser().getId().equals(user.getId()));
    }
}