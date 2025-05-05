package com.freelancer.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for project budget summary information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetSummaryDto {
    
    // Project identifiers
    private Long projectId;
    private String projectName;
    
    // Budget figures
    private BigDecimal totalBudget;
    private BigDecimal invoicedAmount;
    private BigDecimal paidAmount;
    private BigDecimal pendingPaymentAmount;
    private BigDecimal unbilledAmount;
    private BigDecimal remainingBudget;
    
    // Budget metrics
    private double budgetUtilizationPercentage;
    private boolean isOverBudget;
    private LocalDate projectedBudgetDepletion; 
    
    // Time tracking
    private Long totalTrackedHours;
    private Long totalBillableHours;
    private Long unbilledHours;
    
    // Financial summary
    private BigDecimal hourlyRateAverage;
    private BigDecimal estimatedRemainingHours;
    private BigDecimal estimatedTotalCost;
    private BigDecimal profitMargin;
}