package com.freelancer.portal.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a notification in the freelancing platform.
 * <p>
 * Notifications inform users about important events in the system such as
 * new messages, invoice updates, project changes, and client activities.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who should receive this notification.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User recipient;

    /**
     * The type of notification.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    /**
     * The title of the notification.
     */
    @Column(name = "title", nullable = false)
    private String title;

    /**
     * The message content of the notification.
     */
    @Column(name = "content", length = 500)
    private String content;

    /**
     * The related entity type (e.g., 'invoice', 'project', 'message', etc.)
     */
    @Column(name = "entity_type")
    private String entityType;

    /**
     * The ID of the related entity.
     */
    @Column(name = "entity_id")
    private Long entityId;

    /**
     * The URL to redirect to when clicking the notification.
     */
    @Column(name = "link_url")
    private String linkUrl;

    /**
     * Whether the notification has been read by the recipient.
     */
    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    /**
     * When the notification was read.
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * Timestamp when the notification was created.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Mark the notification as read.
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    /**
     * Types of notifications in the system.
     */
    public enum NotificationType {
        INVOICE_CREATED,
        INVOICE_SENT,
        INVOICE_PAID,
        INVOICE_OVERDUE,
        PAYMENT_RECEIVED,
        PROJECT_CREATED,
        PROJECT_UPDATED,
        PROJECT_COMPLETED,
        CLIENT_CREATED,
        CLIENT_UPDATED,
        MESSAGE_RECEIVED,
        TASK_ASSIGNED,
        TASK_COMPLETED,
        SYSTEM_NOTIFICATION
    }
}