package com.freelancer.portal.repository;

import com.freelancer.portal.model.Conversation;
import com.freelancer.portal.model.Project;
import com.freelancer.portal.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Page<Conversation> findByParticipantsContains(User user, Pageable pageable);
    Optional<Conversation> findByIdAndParticipantsContains(Long id, User user);
    
    // New method to find conversations by project
    List<Conversation> findByProject(Project project);
    
    // Count conversations by project
    long countByProject(Project project);
}