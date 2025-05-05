package com.freelancer.portal.service;

import com.freelancer.portal.dto.InvoiceNotificationDto;
import com.freelancer.portal.dto.NotificationDto;
import com.freelancer.portal.model.Invoice;
import com.freelancer.portal.model.Message;
import com.freelancer.portal.model.Notification.NotificationType;
import com.freelancer.portal.model.Project;
import com.freelancer.portal.model.Client;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service for handling notifications in the system.
 */
public interface NotificationService {
    
    /**
     * Send an email notification about an overdue invoice.
     * 
     * @param notification The invoice notification details
     * @return true if the notification was sent successfully
     */
    boolean sendInvoiceOverdueNotification(InvoiceNotificationDto notification);
    
    /**
     * Send reminder notifications for all overdue invoices.
     * 
     * @return The number of notifications sent
     */
    int sendOverdueAlerts();
    
    /**
     * Get notifications for the current user.
     * 
     * @param pageable Pagination information
     * @return A page of notification DTOs
     */
    Page<NotificationDto> getCurrentUserNotifications(Pageable pageable);
    
    /**
     * Get unread notifications for the current user.
     * 
     * @param pageable Pagination information
     * @return A page of notification DTOs
     */
    Page<NotificationDto> getCurrentUserUnreadNotifications(Pageable pageable);
    
    /**
     * Count unread notifications for the current user.
     * 
     * @return The count of unread notifications
     */
    long countCurrentUserUnreadNotifications();
    
    /**
     * Mark a notification as read.
     * 
     * @param id The ID of the notification to mark as read
     * @return The updated notification DTO
     */
    NotificationDto markNotificationAsRead(Long id);
    
    /**
     * Mark all notifications for the current user as read.
     * 
     * @return The number of notifications marked as read
     */
    int markAllNotificationsAsRead();
    
    /**
     * Create a notification for invoice-related events.
     * 
     * @param invoice The invoice
     * @param type The type of notification
     */
    void createInvoiceNotification(Invoice invoice, NotificationType type);
    
    /**
     * Create a notification for project-related events.
     * 
     * @param project The project
     * @param type The type of notification
     */
    void createProjectNotification(Project project, NotificationType type);
    
    /**
     * Create a notification for client-related events.
     * 
     * @param client The client
     * @param type The type of notification
     */
    void createClientNotification(Client client, NotificationType type);
    
    /**
     * Create a notification for message-related events.
     * 
     * @param message The message
     */
    void createMessageNotification(Message message);
}