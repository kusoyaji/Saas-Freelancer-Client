package com.freelancer.portal.mapper;

import com.freelancer.portal.dto.NotificationDto;
import com.freelancer.portal.model.Notification;

/**
 * Utility class for mapping Notification entities to DTOs and vice versa.
 */
public class NotificationMapper {

    /**
     * Maps a Notification entity to a NotificationDto.
     *
     * @param notification the notification entity
     * @return the notification DTO
     */
    public static NotificationDto toDto(Notification notification) {
        if (notification == null) {
            return null;
        }
        
        return NotificationDto.builder()
                .id(notification.getId())
                .recipientId(notification.getRecipient() != null ? notification.getRecipient().getId() : null)
                .type(notification.getType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .entityType(notification.getEntityType())
                .entityId(notification.getEntityId())
                .linkUrl(notification.getLinkUrl())
                .isRead(notification.isRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}