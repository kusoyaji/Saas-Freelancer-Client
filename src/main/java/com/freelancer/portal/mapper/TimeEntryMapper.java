package com.freelancer.portal.mapper;

import com.freelancer.portal.dto.TimeEntryDto;
import com.freelancer.portal.model.Invoice;
import com.freelancer.portal.model.Project;
import com.freelancer.portal.model.TimeEntry;
import com.freelancer.portal.model.User;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for mapping between TimeEntry entity and TimeEntryDto.
 */
public class TimeEntryMapper {

    /**
     * Convert TimeEntry entity to TimeEntryDto.
     *
     * @param timeEntry the time entry entity
     * @return the time entry DTO
     */
    public static TimeEntryDto toDto(TimeEntry timeEntry) {
        if (timeEntry == null) {
            return null;
        }
        
        TimeEntryDto dto = TimeEntryDto.builder()
                .id(timeEntry.getId())
                .projectId(timeEntry.getProject() != null ? timeEntry.getProject().getId() : null)
                .projectName(timeEntry.getProject() != null ? timeEntry.getProject().getName() : null)
                .userId(timeEntry.getUser() != null ? timeEntry.getUser().getId() : null)
                .userName(timeEntry.getUser() != null ? 
                        (timeEntry.getUser().getFirstName() != null ? timeEntry.getUser().getFirstName() : "") + " " + 
                        (timeEntry.getUser().getLastName() != null ? timeEntry.getUser().getLastName() : "").trim() : null)
                .description(timeEntry.getDescription())
                .startTime(timeEntry.getStartTime())
                .endTime(timeEntry.getEndTime())
                // Use the entity's calculated duration (seconds) directly
                .durationSeconds(timeEntry.getDuration()) 
                .hours(timeEntry.getHours() != null ? timeEntry.getHours() : 0.0)
                .billable(timeEntry.isBillable())
                .billed(timeEntry.isBilled())
                .invoiceId(timeEntry.getInvoice() != null ? timeEntry.getInvoice().getId() : null)
                .invoiceNumber(timeEntry.getInvoice() != null ? timeEntry.getInvoice().getInvoiceNumber() : null)
                .createdAt(timeEntry.getCreatedAt())
                .updatedAt(timeEntry.getUpdatedAt())
                .build();
        
        // Remove redundant calculation
        // if (timeEntry.getStartTime() != null && timeEntry.getEndTime() != null) {
        //     long seconds = java.time.Duration.between(timeEntry.getStartTime(), timeEntry.getEndTime()).getSeconds();
        //     dto.setDurationSeconds(seconds);
        // }
        
        return dto;
    }
    
    /**
     * Convert a list of TimeEntry entities to DTOs.
     *
     * @param timeEntries the list of time entry entities
     * @return a list of time entry DTOs
     */
    public static List<TimeEntryDto> toDtoList(List<TimeEntry> timeEntries) {
        if (timeEntries == null) {
            return List.of();
        }
        
        return timeEntries.stream()
                .map(TimeEntryMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert TimeEntryDto to TimeEntry entity.
     *
     * @param timeEntryDto the time entry DTO
     * @param project the project entity
     * @param user the user entity
     * @return the time entry entity
     */
    public static TimeEntry toEntity(TimeEntryDto timeEntryDto, Project project, User user) {
        if (timeEntryDto == null) {
            return null;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        TimeEntry timeEntry = TimeEntry.builder()
                .project(project)
                .user(user)
                .description(timeEntryDto.getDescription())
                .startTime(timeEntryDto.getStartTime())
                .endTime(timeEntryDto.getEndTime())
                .billable(timeEntryDto.getBillable() != null ? timeEntryDto.getBillable() : false)
                .billed(timeEntryDto.getBilled() != null ? timeEntryDto.getBilled() : false)
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        // Calculate hours if start and end time are provided
        if (timeEntry.getStartTime() != null && timeEntry.getEndTime() != null) {
            double hours = calculateHours(timeEntry.getStartTime(), timeEntry.getEndTime());
            timeEntry.setHours(hours);
        } else if (timeEntryDto.getHours() != null) {
            timeEntry.setHours(timeEntryDto.getHours());
        }
        
        return timeEntry;
    }
    
    /**
     * Update TimeEntry entity from DTO.
     *
     * @param timeEntry the time entry entity to update
     * @param timeEntryDto the time entry DTO with new values
     * @param project the project entity (can be null if not changing)
     * @param invoice the invoice entity (can be null if not changed)
     * @return the updated time entry entity
     */
    public static TimeEntry updateFromDto(TimeEntry timeEntry, TimeEntryDto timeEntryDto, Project project, Invoice invoice) {
        if (timeEntry != null && timeEntryDto != null) {
            // Update description if provided
            if (timeEntryDto.getDescription() != null) {
                timeEntry.setDescription(timeEntryDto.getDescription());
            }
            
            // Update project if provided
            if (project != null) {
                timeEntry.setProject(project);
            }
            
            // Update times if provided
            if (timeEntryDto.getStartTime() != null) {
                timeEntry.setStartTime(timeEntryDto.getStartTime());
            }
            
            if (timeEntryDto.getEndTime() != null) {
                timeEntry.setEndTime(timeEntryDto.getEndTime());
            }
            
            // Recalculate hours if both start and end time are present
            if (timeEntry.getStartTime() != null && timeEntry.getEndTime() != null) {
                double hours = calculateHours(timeEntry.getStartTime(), timeEntry.getEndTime());
                timeEntry.setHours(hours);
            } else if (timeEntryDto.getHours() != null) {
                timeEntry.setHours(timeEntryDto.getHours());
            }
            
            // Update billable status if provided
            if (timeEntryDto.getBillable() != null) {
                timeEntry.setBillable(timeEntryDto.getBillable());
            }
            
            // Update billed status if provided
            if (timeEntryDto.getBilled() != null) {
                timeEntry.setBilled(timeEntryDto.getBilled());
            }
            
            // Update invoice if provided
            if (invoice != null) {
                timeEntry.setInvoice(invoice);
            }
            
            // Update the updatedAt timestamp
            timeEntry.setUpdatedAt(LocalDateTime.now());
        }
        
        return timeEntry;
    }
    
    /**
     * Calculate hours between two timestamps.
     *
     * @param startTime the start time
     * @param endTime the end time
     * @return number of hours as a double with precision to 2 decimal places
     */
    private static double calculateHours(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null || endTime.isBefore(startTime)) {
            return 0.0;
        }
        
        Duration duration = Duration.between(startTime, endTime);
        double hours = duration.toMinutes() / 60.0;
        
        // Round to 2 decimal places
        return Math.round(hours * 100.0) / 100.0;
    }
}