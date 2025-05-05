package com.freelancer.portal.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.freelancer.portal.mapper.FlexibleLocalDateTimeDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for time entry information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeEntryDto {
    
    private Long id;
    
    @NotNull(message = "Project ID is required")
    private Long projectId;
    private String invoiceNumber;
    private String projectName;
    
    private Long userId;
    
    private String userName;
    
    @NotNull(message = "Description is required")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime startTime;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime endTime;
    
    // Duration in minutes (legacy field)
    private Integer duration;
    
    // Duration in seconds (more precise)
    private Long durationSeconds;
    
    // Duration in hours (for display and calculations)
    private Double hours;
    
    private Boolean billable;
    
    private Double hourlyRate;
    
    private Double billableAmount;
    
    private Long clientId;
    
    private String clientName;
    
    private Long invoiceId;
    
    private Boolean billed;
    
    private String notes;
    
    private String[] tags;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}