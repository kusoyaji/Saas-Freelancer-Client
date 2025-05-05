package com.freelancer.portal.repository;

import com.freelancer.portal.model.Notification;
import com.freelancer.portal.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Find all notifications for a specific user
     * 
     * @param recipient the user to find notifications for
     * @param pageable pagination information
     * @return page of notifications
     */
    Page<Notification> findByRecipientOrderByCreatedAtDesc(User recipient, Pageable pageable);
    
    /**
     * Find all unread notifications for a specific user
     * 
     * @param recipient the user to find unread notifications for
     * @param pageable pagination information
     * @return page of unread notifications
     */
    Page<Notification> findByRecipientAndIsReadFalseOrderByCreatedAtDesc(User recipient, Pageable pageable);
    
    /**
     * Count unread notifications for a specific user
     * 
     * @param recipient the user to count unread notifications for
     * @return count of unread notifications
     */
    long countByRecipientAndIsReadFalse(User recipient);
    
    /**
     * Mark all unread notifications as read for a specific user
     * 
     * @param recipient the user to mark notifications as read for
     * @return number of notifications marked as read
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.recipient = :recipient AND n.isRead = false")
    int markAllAsReadForUser(@Param("recipient") User recipient);
}