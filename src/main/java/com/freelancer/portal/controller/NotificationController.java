package com.freelancer.portal.controller;

import com.freelancer.portal.dto.NotificationDto;
import com.freelancer.portal.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing notifications.
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    
    /**
     * GET /notifications : Get all notifications for the current user.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and a page of notifications in the body
     */
    @GetMapping
    public ResponseEntity<Page<NotificationDto>> getNotifications(Pageable pageable) {
        log.debug("REST request to get notifications for current user");
        return ResponseEntity.ok(notificationService.getCurrentUserNotifications(pageable));
    }
    
    /**
     * GET /notifications/unread : Get unread notifications for the current user.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and a page of unread notifications in the body
     */
    @GetMapping("/unread")
    public ResponseEntity<Page<NotificationDto>> getUnreadNotifications(Pageable pageable) {
        log.debug("REST request to get unread notifications for current user");
        return ResponseEntity.ok(notificationService.getCurrentUserUnreadNotifications(pageable));
    }
    
    /**
     * GET /notifications/unread/count : Get the count of unread notifications for the current user.
     *
     * @return the ResponseEntity with status 200 (OK) and the count in the body
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadNotificationsCount() {
        log.debug("REST request to count unread notifications for current user");
        Long count = notificationService.countCurrentUserUnreadNotifications();
        log.debug("Returning unread count: {}", count);
        return ResponseEntity.ok(count);
    }
    
    /**
     * PUT /notifications/{id}/read : Mark a notification as read.
     *
     * @param id the id of the notification to mark as read
     * @return the ResponseEntity with status 200 (OK) and the updated notification in the body
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationDto> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markNotificationAsRead(id));
    }
    
    /**
     * PUT /notifications/read-all : Mark all notifications as read.
     *
     * @return the ResponseEntity with status 200 (OK) and the number of notifications marked as read
     */
    @PutMapping("/read-all")
    public ResponseEntity<Integer> markAllAsRead() {
        return ResponseEntity.ok(notificationService.markAllNotificationsAsRead());
    }
}