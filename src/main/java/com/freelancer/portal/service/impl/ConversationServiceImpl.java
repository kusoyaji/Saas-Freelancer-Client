package com.freelancer.portal.service.impl;

import com.freelancer.portal.dto.ConversationDto;
import com.freelancer.portal.mapper.ConversationMapper;
import com.freelancer.portal.model.Conversation;
import com.freelancer.portal.model.User;
import com.freelancer.portal.repository.ConversationRepository;
import com.freelancer.portal.repository.MessageRepository;
import com.freelancer.portal.repository.UserRepository;
import com.freelancer.portal.service.ConversationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    @Override
    @Transactional
    public ConversationDto createConversation(List<Long> participantIds) {
        User currentUser = getCurrentUser();
        
        // Ensure current user is included in participants
        if (!participantIds.contains(currentUser.getId())) {
            participantIds.add(currentUser.getId());
        }
        
        // Get all participants
        List<User> participants = userRepository.findAllById(participantIds);
        
        // Verify all participants exist
        if (participants.size() != participantIds.size()) {
            throw new EntityNotFoundException("One or more participants not found");
        }
        
        // Create the conversation with title based on participants
        String title = generateConversationTitle(participants, currentUser);
        
        Conversation conversation = Conversation.builder()
                .title(title)
                .participants(new HashSet<>(participants))
                .build();
        
        Conversation savedConversation = conversationRepository.save(conversation);
        return ConversationMapper.toDto(savedConversation, currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationDto> getConversationsForUser(Long userId, Pageable pageable) {
        User user = getCurrentUser();
        
        // If userId is provided and is not the current user, verify authorization
        if (userId != null && !userId.equals(user.getId())) {
            // In a real app, check if the user has admin permissions here
            throw new EntityNotFoundException("Unauthorized to view conversations for user: " + userId);
        }
        
        Page<Conversation> conversations = conversationRepository.findByParticipantsContains(user, pageable);
        return conversations.map(conversation -> ConversationMapper.toDto(conversation, user.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationDto getConversationById(Long id) {
        User currentUser = getCurrentUser();
        Conversation conversation = conversationRepository.findByIdAndParticipantsContains(id, currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found with id: " + id));
        
        return ConversationMapper.toDto(conversation, currentUser.getId());
    }

    @Override
    @Transactional
    public ConversationDto addParticipant(Long conversationId, Long userId) {
        User currentUser = getCurrentUser();
        Conversation conversation = conversationRepository.findByIdAndParticipantsContains(conversationId, currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found with id: " + conversationId));
        
        User userToAdd = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        // Add user to participants
        conversation.getParticipants().add(userToAdd);
        Conversation updatedConversation = conversationRepository.save(conversation);
        
        return ConversationMapper.toDto(updatedConversation, currentUser.getId());
    }

    @Override
    @Transactional
    public ConversationDto removeParticipant(Long conversationId, Long userId) {
        User currentUser = getCurrentUser();
        Conversation conversation = conversationRepository.findByIdAndParticipantsContains(conversationId, currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found with id: " + conversationId));
        
        // Cannot remove the last participant
        if (conversation.getParticipants().size() <= 2) {
            throw new IllegalStateException("Cannot remove participant from conversation with only two participants");
        }
        
        // Find user to remove
        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        // Check if removing self
        if (userId.equals(currentUser.getId())) {
            conversation.getParticipants().remove(currentUser);
            conversationRepository.save(conversation);
            return null; // Indicating user left the conversation
        } else {
            // Check if authorized to remove others (in a real app, check admin rights)
            conversation.getParticipants().remove(userToRemove);
            Conversation updatedConversation = conversationRepository.save(conversation);
            return ConversationMapper.toDto(updatedConversation, currentUser.getId());
        }
    }

    @Override
    @Transactional
    public void deleteConversation(Long id) {
        User currentUser = getCurrentUser();
        Conversation conversation = conversationRepository.findByIdAndParticipantsContains(id, currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found with id: " + id));
        
        // In a real app, consider soft delete or checking if user has admin rights
        // For now, a participant can delete a conversation
        conversationRepository.delete(conversation);
    }
    
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
    
    private String generateConversationTitle(List<User> participants, User currentUser) {
        // Filter out current user for the title
        List<String> names = participants.stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .map(user -> user.getFirstName() + " " + user.getLastName())
                .toList();
        
        // If no other participants (should not happen), use "New Conversation"
        if (names.isEmpty()) {
            return "New Conversation";
        }
        
        // If only one other participant, use their name
        if (names.size() == 1) {
            return names.get(0);
        }
        
        // For group conversations, combine names (limit to 3 names for brevity)
        String namesList = names.stream().limit(3).reduce((a, b) -> a + ", " + b).orElse("");
        
        // If more than 3 participants, add "and X others"
        if (names.size() > 3) {
            namesList += " and " + (names.size() - 3) + " others";
        }
        
        return namesList;
    }
}