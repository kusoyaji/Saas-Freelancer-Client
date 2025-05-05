package com.freelancer.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Transfer Object for time entry summary information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeEntrySummaryDto {
    
    // Project identification
    private Long projectId;
    private String projectName;
    private Double billableAmount;
    // Time metrics
    private Double totalHours;
    private Double billableHours;
    private Double nonBillableHours;
    private Double billedHours;
    private Double unbilledHours;
    
    // Financial metrics
    private BigDecimal hourlyRate;
    private BigDecimal totalBillableAmount;
    private BigDecimal billedAmount;
    private BigDecimal unbilledAmount;
    
    // Recent entries
    private List<TimeEntryDto> recentEntries;
    
    // User breakdown
    private List<UserTimeBreakdownDto> userBreakdown;
    
    /**
     * Inner class for user time breakdown.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserTimeBreakdownDto {
        private Long userId;
        private String userName;
        private Double hours;
        private Double percentage;
    }
}