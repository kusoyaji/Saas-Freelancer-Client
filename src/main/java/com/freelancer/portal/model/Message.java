package com.freelancer.portal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Entity representing a message in the freelancing platform.
 * <p>
 * Messages are the individual communications sent within either project contexts or
 * standalone conversations. They support both text content and file attachments, and 
 * can track whether they've been read by recipients.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The text content of the message.
     */
    @NotBlank(message = "Message content is required")
    @Size(max = 5000, message = "Message content cannot exceed 5000 characters")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * The project this message is associated with (if any).
     */
    @JsonBackReference("project-messages")
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    /**
     * The conversation this message is associated with (if any).
     */
    @JsonBackReference("conversation-messages")
    @NotNull(message = "Conversation is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    /**
     * The user who sent this message.
     */
    @NotNull(message = "Sender is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * Flag indicating whether this message has been read by the recipient.
     */
    @Column(name = "is_read")
    private Boolean isRead;
    
    /**
     * URL to any file attached to this message.
     */
    @Size(max = 1000, message = "File attachment URL cannot exceed 1000 characters")
    @Column(name = "file_attachment_url")
    private String fileAttachmentUrl;

    /**
     * Timestamp when the message was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the message was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        sentAt = now;
        if (isRead == null) {
            isRead = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Mark the message as read.
     */
    public void markAsRead() {
        if (!isRead) {
            isRead = true;
            readAt = LocalDateTime.now();
        }
    }
}