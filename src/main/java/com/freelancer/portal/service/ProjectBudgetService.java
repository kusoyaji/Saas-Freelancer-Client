package com.freelancer.portal.service;

import com.freelancer.portal.dto.ProjectBudgetSummaryDto;

import java.math.BigDecimal;

/**
 * Service for managing project budget operations.
 */
public interface ProjectBudgetService {

    /**
     * Get comprehensive budget summary for a project.
     *
     * @param projectId the project ID
     * @return the project budget summary
     */
    ProjectBudgetSummaryDto getProjectBudgetSummary(Long projectId);
    
    /**
     * Calculate total invoiced amount for a project.
     *
     * @param projectId the project ID
     * @return the total invoiced amount
     */
    BigDecimal calculateTotalInvoicedAmount(Long projectId);
    
    /**
     * Calculate total paid amount for a project.
     *
     * @param projectId the project ID
     * @return the total paid amount
     */
    BigDecimal calculateTotalPaidAmount(Long projectId);
    
    /**
     * Calculate total pending payment amount for a project.
     *
     * @param projectId the project ID
     * @return the total pending amount
     */
    BigDecimal calculateTotalPendingAmount(Long projectId);
    
    /**
     * Calculate budget utilization percentage for a project.
     *
     * @param projectId the project ID
     * @return the budget utilization percentage (0-100)
     */
    double calculateBudgetUtilizationPercentage(Long projectId);
    
    /**
     * Calculate unbilled amount for a project based on billable time entries.
     *
     * @param projectId the project ID
     * @return the unbilled amount
     */
    BigDecimal calculateUnbilledAmount(Long projectId);
    
    /**
     * Check if a project is over budget.
     *
     * @param projectId the project ID
     * @return true if the project is over budget, false otherwise
     */
    boolean isOverBudget(Long projectId);
    
    /**
     * Calculate remaining budget for a project.
     *
     * @param projectId the project ID
     * @return the remaining budget amount
     */
    BigDecimal calculateRemainingBudget(Long projectId);
    
    /**
     * Check if project expenses are on track with project duration.
     *
     * @param projectId the project ID
     * @return positive percentage if over budget based on timeline, negative if under
     */
    double calculateBudgetDeviationByTimeline(Long projectId);
}