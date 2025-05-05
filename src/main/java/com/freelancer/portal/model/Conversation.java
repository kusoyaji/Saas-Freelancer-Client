package com.freelancer.portal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Objects;

/**
 * Entity representing a conversation between users in the freelancing platform.
 * <p>
 * Conversations allow for direct messaging between freelancers and clients,
 * providing a communication channel outside the context of specific projects.
 * Each conversation involves multiple participants and contains a series of messages.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "conversations")
@EqualsAndHashCode(exclude = {"participants", "messages"})
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    /**
     * Users who participate in this conversation.
     */
    @JsonManagedReference
    @ManyToMany
    @JoinTable(
        name = "conversation_participants",
        joinColumns = @JoinColumn(name = "conversation_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> participants = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    /**
     * Messages contained in this conversation.
     */
    @JsonManagedReference("conversation-messages")
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    /**
     * Timestamp when the conversation was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the conversation was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        lastMessageAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Custom hashCode implementation to avoid circular references
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, title, createdAt);
    }

    /**
     * Custom equals implementation to avoid circular references
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Conversation that = (Conversation) obj;
        return Objects.equals(id, that.id);
    }

    /**
     * Add a participant to the conversation.
     *
     * @param user the user to add
     */
    public void addParticipant(User user) {
        participants.add(user);
    }

    /**
     * Remove a participant from the conversation.
     *
     * @param user the user to remove
     */
    public void removeParticipant(User user) {
        participants.remove(user);
    }

    /**
     * Add a message to the conversation.
     *
     * @param message the message to add
     */
    public void addMessage(Message message) {
        messages.add(message);
        message.setConversation(this);
        updatedAt = LocalDateTime.now();
    }

    /**
     * Remove a message from the conversation.
     *
     * @param message the message to remove
     */
    public void removeMessage(Message message) {
        messages.remove(message);
        message.setConversation(null);
    }
}