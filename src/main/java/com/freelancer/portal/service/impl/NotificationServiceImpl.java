package com.freelancer.portal.service.impl;

import com.freelancer.portal.controller.WebSocketMessageController;
import com.freelancer.portal.dto.InvoiceNotificationDto;
import com.freelancer.portal.dto.NotificationDto;
import com.freelancer.portal.mapper.NotificationMapper;
import com.freelancer.portal.model.Client;
import com.freelancer.portal.model.Invoice;
import com.freelancer.portal.model.Message;
import com.freelancer.portal.model.Notification;
import com.freelancer.portal.model.Notification.NotificationType;
import com.freelancer.portal.model.Project;
import com.freelancer.portal.model.User;
import com.freelancer.portal.repository.InvoiceRepository;
import com.freelancer.portal.repository.NotificationRepository;
import com.freelancer.portal.repository.UserRepository;
import com.freelancer.portal.service.NotificationService;
import org.springframework.context.ApplicationContext;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;



/**
 * Implementation of the NotificationService interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final InvoiceRepository invoiceRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ApplicationContext applicationContext;
    
    @Override
    public boolean sendInvoiceOverdueNotification(InvoiceNotificationDto notification) {
        try {
            // In a real implementation, this would use an email service or messaging queue
            log.info("Sending overdue notification for invoice: {}", notification.getInvoiceNumber());
            log.info("To: {} <{}>", notification.getClientName(), notification.getClientEmail());
            log.info("Subject: Invoice #{} is overdue by {} days", 
                    notification.getInvoiceNumber(), notification.getDaysOverdue());
            log.info("Message: Your payment of ${} for project '{}' was due on {}. Please make your payment as soon as possible.",
                    notification.getAmount(), 
                    notification.getProjectName(),
                    notification.getDueDate());
            
            return true;
        } catch (Exception e) {
            log.error("Failed to send notification for invoice #{}: {}", 
                    notification.getInvoiceNumber(), e.getMessage());
            return false;
        }
    }
    
    /**
     * BUSINESS LOGIC: Query for overdue invoices and send notifications.
     * This method demonstrates checking for invoices past their due date
     * and sending notification alerts to clients.
     */
    @Override
    @Scheduled(cron = "0 0 8 * * ?") // Run daily at 8:00 AM
    @Transactional
    public int sendOverdueAlerts() {
        LocalDateTime now = LocalDateTime.now();
        
        // Get all invoices with status SENT or OVERDUE and due date in the past
        List<Invoice> overdueInvoices = invoiceRepository.findByStatusInAndDueDateBefore(
                List.of(Invoice.Status.SENT, Invoice.Status.OVERDUE), 
                now);
        
        int notificationsSent = 0;
        List<Long> successfulIds = new ArrayList<>();
        
        for (Invoice invoice : overdueInvoices) {
            try {
                // Calculate days overdue
                long daysOverdue = ChronoUnit.DAYS.between(invoice.getDueDate(), now);
                
                // Only send notifications for invoices that are overdue by at least 1 day
                if (daysOverdue >= 1) {
                    Project project = invoice.getProject();
                    
                    // Create notification dto
                    InvoiceNotificationDto notification = InvoiceNotificationDto.builder()
                            .invoiceId(invoice.getId())
                            .invoiceNumber(invoice.getInvoiceNumber())
                            .clientName(project.getClient().getName())
                            .clientEmail(project.getClient().getEmail())
                            .amount(invoice.getAmount())
                            .dueDate(invoice.getDueDate())
                            .daysOverdue(daysOverdue)
                            .projectName(project.getName())
                            .build();
                    
                    // Send email notification
                    boolean sent = sendInvoiceOverdueNotification(notification);
                    
                    if (sent) {
                        notificationsSent++;
                        successfulIds.add(invoice.getId());
                        
                        // Update invoice status to OVERDUE if it's not already
                        if (invoice.getStatus() != Invoice.Status.OVERDUE) {
                            invoice.setStatus(Invoice.Status.OVERDUE);
                            invoiceRepository.save(invoice);
                        }
                        
                        // Create in-app notification
                        createInvoiceNotification(invoice, NotificationType.INVOICE_OVERDUE);
                    }
                }
            } catch (Exception e) {
                log.error("Error processing notification for invoice #{}: {}", 
                        invoice.getInvoiceNumber(), e.getMessage());
            }
        }
        
        // Log summary
        if (notificationsSent > 0) {
            log.info("Sent {} overdue invoice notifications", notificationsSent);
        }
        
        return notificationsSent;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> getCurrentUserNotifications(Pageable pageable) {
        User currentUser = getCurrentUser();
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(currentUser, pageable)
                .map(NotificationMapper::toDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> getCurrentUserUnreadNotifications(Pageable pageable) {
        User currentUser = getCurrentUser();
        return notificationRepository.findByRecipientAndIsReadFalseOrderByCreatedAtDesc(currentUser, pageable)
                .map(NotificationMapper::toDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countCurrentUserUnreadNotifications() {
        User currentUser = getCurrentUser();
        return notificationRepository.countByRecipientAndIsReadFalse(currentUser);
    }
    
    @Override
    @Transactional
    public NotificationDto markNotificationAsRead(Long id) {
        User currentUser = getCurrentUser();
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found with id: " + id));
        
        if (!notification.getRecipient().getId().equals(currentUser.getId())) {
            throw new EntityNotFoundException("Notification not found with id: " + id);
        }
        
        notification.markAsRead();
        notification = notificationRepository.save(notification);
        
        return NotificationMapper.toDto(notification);
    }

    @Override
    @Transactional
    public int markAllNotificationsAsRead() {
        User currentUser = getCurrentUser();
        
        // Use the repository method that performs a batch update
        int updatedCount = notificationRepository.markAllAsReadForUser(currentUser);
        
        // Log the operation
        log.info("Marked {} notifications as read for user {}", updatedCount, currentUser.getEmail());
        
        return updatedCount;
    }
    
    @Override
    @Transactional
    public void createInvoiceNotification(Invoice invoice, NotificationType type) {
        String title;
        String content;
        User recipient;
        String entityType = "invoice";
        String linkUrl = "/invoices/" + invoice.getId();
        
        switch (type) {
            case INVOICE_CREATED:
                title = "New Invoice Created";
                content = "Invoice #" + invoice.getInvoiceNumber() + " has been created for " + 
                         (invoice.getProject() != null ? "project '" + invoice.getProject().getName() + "'" : "your services");
                recipient = invoice.getFreelancer();
                break;
            case INVOICE_SENT:
                title = "Invoice Sent";
                content = "Invoice #" + invoice.getInvoiceNumber() + " for $" + invoice.getAmount() + 
                          " has been sent to " + invoice.getClient().getName();
                recipient = invoice.getFreelancer();
                break;
            case INVOICE_PAID:
                title = "Invoice Paid";
                content = "Invoice #" + invoice.getInvoiceNumber() + " for $" + invoice.getAmount() + 
                          " has been paid by " + invoice.getClient().getName();
                recipient = invoice.getFreelancer();
                break;
            case INVOICE_OVERDUE:
                title = "Invoice Overdue";
                content = "Invoice #" + invoice.getInvoiceNumber() + " for $" + invoice.getAmount() + 
                          " is overdue. Due date was " + invoice.getDueDate();
                recipient = invoice.getClient().getUser();
                break;
            default:
                title = "Invoice Update";
                content = "There has been an update to invoice #" + invoice.getInvoiceNumber();
                recipient = invoice.getFreelancer();
        }
        
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .content(content)
                .entityType(entityType)
                .entityId(invoice.getId())
                .linkUrl(linkUrl)
                .build();
        
        notification = notificationRepository.save(notification);
        
        // Send the notification via WebSocket
        try {
            // Get the WebSocketMessageController bean to send the notification
            WebSocketMessageController webSocketController = applicationContext.getBean(WebSocketMessageController.class);
            
            // Convert to DTO and send via WebSocket
            NotificationDto notificationDto = NotificationMapper.toDto(notification);
            webSocketController.sendNotificationToUser(recipient.getId().toString(), notificationDto);
            
            log.info("Sent invoice notification via WebSocket: {}", title);
        } catch (Exception e) {
            log.error("Failed to send notification via WebSocket: {}", e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public void createProjectNotification(Project project, NotificationType type) {
        String title;
        String content;
        User recipient;
        String entityType = "project";
        String linkUrl = "/projects/" + project.getId();
        
        switch (type) {
            case PROJECT_CREATED:
                title = "New Project Created";
                content = "Project '" + project.getName() + "' has been created with " + project.getClient().getName();
                recipient = project.getFreelancer();
                break;
            case PROJECT_UPDATED:
                title = "Project Updated";
                content = "Project '" + project.getName() + "' has been updated";
                recipient = project.getClient().getUser();
                break;
            case PROJECT_COMPLETED:
                title = "Project Completed";
                content = "Project '" + project.getName() + "' has been marked as completed";
                recipient = project.getClient().getUser();
                break;
            default:
                title = "Project Update";
                content = "There has been an update to project '" + project.getName() + "'";
                recipient = project.getFreelancer();
        }
        
        // Skip notification creation if recipient is null
        if (recipient == null) {
            log.warn("Skipping notification creation for project {} (type: {}) due to null recipient", 
                project.getId(), type);
            return;
        }
        
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .content(content)
                .entityType(entityType)
                .entityId(project.getId())
                .linkUrl(linkUrl)
                .build();
        
        notification = notificationRepository.save(notification);
        
        // Send the notification via WebSocket
        try {
            // Get the WebSocketMessageController bean to send the notification
            WebSocketMessageController webSocketController = applicationContext.getBean(WebSocketMessageController.class);
            
            // Convert to DTO and send via WebSocket
            NotificationDto notificationDto = NotificationMapper.toDto(notification);
            webSocketController.sendNotificationToUser(recipient.getId().toString(), notificationDto);
            
            log.info("Sent project notification via WebSocket: {}", title);
        } catch (Exception e) {
            log.error("Failed to send project notification via WebSocket: {}", e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public void createClientNotification(Client client, NotificationType type) {
        String title;
        String content;
        User recipient = client.getFreelancer(); // Changed to freelancer as they should receive client notifications
        String entityType = "client";
        String linkUrl = "/clients/" + client.getId();
        
        switch (type) {
            case CLIENT_CREATED:
                title = "New Client Added";
                content = "You've added " + client.getName() + " as a new client.";
                break;
            case CLIENT_UPDATED:
                title = "Client Information Updated";
                content = "Information for client " + client.getName() + " has been updated.";
                break;
            default:
                title = "Client Account Update";
                content = "There has been an update to client " + client.getName();
        }
        
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .content(content)
                .entityType(entityType)
                .entityId(client.getId())
                .linkUrl(linkUrl)
                .build();
        
        notification = notificationRepository.save(notification);
        
        // Send the notification via WebSocket
        try {
            // Get the WebSocketMessageController bean to send the notification
            WebSocketMessageController webSocketController = applicationContext.getBean(WebSocketMessageController.class);
            
            // Convert to DTO and send via WebSocket
            NotificationDto notificationDto = NotificationMapper.toDto(notification);
            webSocketController.sendNotificationToUser(recipient.getId().toString(), notificationDto);
            
            log.info("Sent client notification via WebSocket: {}", title);
        } catch (Exception e) {
            log.error("Failed to send client notification via WebSocket: {}", e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public void createMessageNotification(Message message) {
        // Get the recipient of the message
        User recipient;
        
        if (message.getConversation() != null) {
            // For conversation messages, send to all participants except the sender
            for (User participant : message.getConversation().getParticipants()) {
                if (!participant.getId().equals(message.getSender().getId())) {
                    createMessageNotificationForUser(message, participant);
                }
            }
        } else if (message.getProject() != null) {
            // For project messages, determine the recipient based on sender role
            Project project = message.getProject();
            if (message.getSender().getId().equals(project.getFreelancer().getId())) {
                // If sender is freelancer, recipient is client
                recipient = project.getClient().getUser();
                createMessageNotificationForUser(message, recipient);
            } else {
                // If sender is client, recipient is freelancer
                recipient = project.getFreelancer();
                createMessageNotificationForUser(message, recipient);
            }
        }
    }
    
    private void createMessageNotificationForUser(Message message, User recipient) {
        String senderName = message.getSender().getFirstName() + " " + message.getSender().getLastName();
        String title = "New Message from " + senderName;
        String content = message.getContent().length() > 50 
                ? message.getContent().substring(0, 47) + "..." 
                : message.getContent();
        String entityType = "message";
        String linkUrl;
        
        // Determine the appropriate link URL
        if (message.getConversation() != null) {
            linkUrl = "/messages/conversation/" + message.getConversation().getId();
        } else if (message.getProject() != null) {
            linkUrl = "/projects/" + message.getProject().getId() + "/messages";
        } else {
            linkUrl = "/messages";
        }
        
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(NotificationType.MESSAGE_RECEIVED)
                .title(title)
                .content(content)
                .entityType(entityType)
                .entityId(message.getId())
                .linkUrl(linkUrl)
                .build();
        
        notification = notificationRepository.save(notification);
        
        // Send the notification via WebSocket
        try {
            // Get the WebSocketMessageController bean to send the notification
            WebSocketMessageController webSocketController = applicationContext.getBean(WebSocketMessageController.class);
            
            // Convert to DTO and send via WebSocket
            NotificationDto notificationDto = NotificationMapper.toDto(notification);
            webSocketController.sendNotificationToUser(recipient.getId().toString(), notificationDto);
            
            log.info("Sent message notification via WebSocket: {}", title);
        } catch (Exception e) {
            log.error("Failed to send message notification via WebSocket: {}", e.getMessage(), e);
        }
    }
    
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}