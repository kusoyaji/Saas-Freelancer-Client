package com.freelancer.portal.repository;

import com.freelancer.portal.model.Conversation;
import com.freelancer.portal.model.Message;
import com.freelancer.portal.model.Project;
import com.freelancer.portal.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByProject(Project project, Pageable pageable);
    List<Message> findByProjectOrderByCreatedAtDesc(Project project);
    Page<Message> findBySender(User sender, Pageable pageable);
    Page<Message> findByProjectAndIsReadFalseAndSenderNot(Project project, User currentUser, Pageable pageable);
    long countByProjectAndIsReadFalseAndSenderNot(Project project, User currentUser);
    
    // New method to count all messages for a project
    long countByProject(Project project);
    
    // Conversation-based messaging methods
    Page<Message> findByConversation(Conversation conversation, Pageable pageable);
    Page<Message> findByConversationAndIsReadFalseAndSenderNot(Conversation conversation, User currentUser, Pageable pageable);
    long countByConversationAndIsReadFalseAndSenderNot(Conversation conversation, User currentUser);
    Message findFirstByConversationOrderByCreatedAtDesc(Conversation conversation);
}