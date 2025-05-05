package com.freelancer.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Data Transfer Object for comprehensive project budget information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectBudgetSummaryDto {
    
    // Project identification
    private Long projectId;
    private String projectName;
    
    // Budget information
    private BigDecimal totalBudget;
    private BigDecimal remainingBudget;
    private double budgetUtilizationPercentage;
    private boolean isOverBudget;
    
    // Timeline information
    private LocalDate startDate;
    private LocalDate endDate;
    private int projectDurationDays;
    private int daysElapsed;
    private double projectTimePercentElapsed;
    private double budgetDeviationByTimeline;
    
    // Financial metrics
    private BigDecimal invoicedAmount;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    private BigDecimal unbilledAmount;
    private BigDecimal potentialFinalValue;
    
    // Time tracking summary
    private double totalHours;
    private double billableHours;
    private double billedHours;
    private double unbilledHours;
    private BigDecimal hourlyRate;
    
    // Invoice statistics
    private int totalInvoicesCount;
    private int paidInvoicesCount;
    private int pendingInvoicesCount;
    private int overdueInvoicesCount;
    
    // Monthly breakdown
    private List<MonthlyBreakdown> monthlyBreakdown;
    
    /**
     * Monthly financial breakdown.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyBreakdown {
        private String month;
        private BigDecimal invoicedAmount;
        private BigDecimal paidAmount;
        private double hours;
        private BigDecimal totalValue;
    }
}