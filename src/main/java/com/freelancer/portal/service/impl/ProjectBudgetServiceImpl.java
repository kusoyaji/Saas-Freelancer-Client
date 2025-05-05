package com.freelancer.portal.service.impl;

import com.freelancer.portal.dto.ProjectBudgetSummaryDto;
import com.freelancer.portal.exception.ResourceNotFoundException;
import com.freelancer.portal.model.Invoice;
import com.freelancer.portal.model.Payment;
import com.freelancer.portal.model.Project;
import com.freelancer.portal.repository.InvoiceRepository;
import com.freelancer.portal.repository.PaymentRepository;
import com.freelancer.portal.repository.ProjectRepository;
import com.freelancer.portal.repository.TimeEntryRepository;
import com.freelancer.portal.service.ProjectBudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of the project budget service.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectBudgetServiceImpl implements ProjectBudgetService {

    private final ProjectRepository projectRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final TimeEntryRepository timeEntryRepository;

    @Override
    public ProjectBudgetSummaryDto getProjectBudgetSummary(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        
        // Calculate financial metrics
        BigDecimal invoicedAmount = calculateTotalInvoicedAmount(projectId);
        BigDecimal paidAmount = calculateTotalPaidAmount(projectId);
        BigDecimal pendingAmount = calculateTotalPendingAmount(projectId);
        BigDecimal totalBudget = project.getBudget() != null ? project.getBudget() : BigDecimal.ZERO;
        BigDecimal remainingBudget = calculateRemainingBudget(projectId);
        BigDecimal unbilledAmount = calculateUnbilledAmount(projectId);
        
        // Calculate time metrics
        Double totalHours = timeEntryRepository.calculateTotalHoursForProject(projectId);
        Double billableHours = timeEntryRepository.calculateBillableHoursForProject(projectId);
        Double billedHours = timeEntryRepository.calculateBilledHoursForProject(projectId);
        Double unbilledHours = timeEntryRepository.calculateUnbilledHoursForProject(projectId);
        
        // Calculate timeline metrics
        LocalDate startDate = project.getStartDate();
        LocalDate endDate = project.getEndDate();
        LocalDate today = LocalDate.now();
        
        int projectDurationDays = 0;
        int daysElapsed = 0;
        double projectTimePercentElapsed = 0.0;
        
        if (startDate != null && endDate != null) {
            projectDurationDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
            daysElapsed = (int) ChronoUnit.DAYS.between(startDate, 
                    today.isAfter(endDate) ? endDate : today) + 1;
            projectTimePercentElapsed = (double) daysElapsed / projectDurationDays * 100;
        }
        
        // Calculate invoice statistics
        List<Invoice> projectInvoices = invoiceRepository.findByProjectId(projectId);
        int totalInvoicesCount = projectInvoices.size();
        int paidInvoicesCount = (int) projectInvoices.stream()
                .filter(invoice -> invoice.getStatus() == Invoice.Status.PAID)
                .count();
        int pendingInvoicesCount = (int) projectInvoices.stream()
                .filter(invoice -> invoice.getStatus() == Invoice.Status.SENT)
                .count();
        int overdueInvoicesCount = (int) projectInvoices.stream()
                .filter(invoice -> invoice.getStatus() == Invoice.Status.OVERDUE)
                .count();
        
        // Calculate potential final value (invoiced + unbilled)
        BigDecimal potentialFinalValue = invoicedAmount.add(unbilledAmount);
        
        // Calculate budget utilization
        double budgetUtilizationPercentage = calculateBudgetUtilizationPercentage(projectId);
        boolean isOverBudget = budgetUtilizationPercentage > 100;
        
        // Calculate budget deviation by timeline
        double budgetDeviationByTimeline = calculateBudgetDeviationByTimeline(projectId);
        
        // Build monthly breakdown
        List<ProjectBudgetSummaryDto.MonthlyBreakdown> monthlyBreakdown = buildMonthlyBreakdown(projectId);
        
        // Build and return complete summary
        return ProjectBudgetSummaryDto.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .totalBudget(totalBudget)
                .remainingBudget(remainingBudget)
                .budgetUtilizationPercentage(budgetUtilizationPercentage)
                .isOverBudget(isOverBudget)
                .startDate(startDate)
                .endDate(endDate)
                .projectDurationDays(projectDurationDays)
                .daysElapsed(daysElapsed)
                .projectTimePercentElapsed(projectTimePercentElapsed)
                .budgetDeviationByTimeline(budgetDeviationByTimeline)
                .invoicedAmount(invoicedAmount)
                .paidAmount(paidAmount)
                .pendingAmount(pendingAmount)
                .unbilledAmount(unbilledAmount)
                .potentialFinalValue(potentialFinalValue)
                .totalHours(totalHours)
                .billableHours(billableHours)
                .billedHours(billedHours)
                .unbilledHours(unbilledHours)
                .hourlyRate(project.getHourlyRate())
                .totalInvoicesCount(totalInvoicesCount)
                .paidInvoicesCount(paidInvoicesCount)
                .pendingInvoicesCount(pendingInvoicesCount)
                .overdueInvoicesCount(overdueInvoicesCount)
                .monthlyBreakdown(monthlyBreakdown)
                .build();
    }
    
    @Override
    public BigDecimal calculateTotalInvoicedAmount(Long projectId) {
        return invoiceRepository.findByProjectId(projectId).stream()
                .map(Invoice::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Override
    public BigDecimal calculateTotalPaidAmount(Long projectId) {
        return paymentRepository.findByInvoiceProjectId(projectId).stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Override
    public BigDecimal calculateTotalPendingAmount(Long projectId) {
        BigDecimal invoiced = calculateTotalInvoicedAmount(projectId);
        BigDecimal paid = calculateTotalPaidAmount(projectId);
        return invoiced.subtract(paid);
    }
    
    @Override
    public double calculateBudgetUtilizationPercentage(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        
        if (project.getBudget() == null || project.getBudget().compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        
        BigDecimal invoicedAmount = calculateTotalInvoicedAmount(projectId);
        BigDecimal unbilledAmount = calculateUnbilledAmount(projectId);
        BigDecimal totalUsed = invoicedAmount.add(unbilledAmount);
        
        return totalUsed.divide(project.getBudget(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }
    
    @Override
    public BigDecimal calculateUnbilledAmount(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        
        Double unbilledHours = timeEntryRepository.calculateUnbilledHoursForProject(projectId);
        
        if (unbilledHours == null || unbilledHours == 0 || project.getHourlyRate() == null) {
            return BigDecimal.ZERO;
        }
        
        return project.getHourlyRate()
                .multiply(BigDecimal.valueOf(unbilledHours))
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public boolean isOverBudget(Long projectId) {
        return calculateBudgetUtilizationPercentage(projectId) > 100;
    }
    
    @Override
    public BigDecimal calculateRemainingBudget(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        
        if (project.getBudget() == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal invoicedAmount = calculateTotalInvoicedAmount(projectId);
        BigDecimal unbilledAmount = calculateUnbilledAmount(projectId);
        BigDecimal totalUsed = invoicedAmount.add(unbilledAmount);
        
        BigDecimal remaining = project.getBudget().subtract(totalUsed);
        return remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining;
    }
    
    @Override
    public double calculateBudgetDeviationByTimeline(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        
        if (project.getBudget() == null || project.getBudget().compareTo(BigDecimal.ZERO) == 0 || 
            project.getStartDate() == null || project.getEndDate() == null) {
            return 0.0;
        }
        
        LocalDate today = LocalDate.now();
        LocalDate startDate = project.getStartDate();
        LocalDate endDate = project.getEndDate();
        
        // If project hasn't started yet
        if (today.isBefore(startDate)) {
            return 0.0;
        }
        
        // Calculate elapsed project time as percentage
        int totalDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
        int elapsedDays = (int) ChronoUnit.DAYS.between(startDate, 
                today.isAfter(endDate) ? endDate : today) + 1;
        double timePercentage = (double) elapsedDays / totalDays * 100;
        
        // Calculate used budget percentage
        double budgetPercentage = calculateBudgetUtilizationPercentage(projectId);
        
        // Return the difference (positive if over budget based on time, negative if under)
        return budgetPercentage - timePercentage;
    }
    
    /**
     * Build monthly breakdown of financial data.
     *
     * @param projectId the project ID
     * @return list of monthly breakdowns
     */
    private List<ProjectBudgetSummaryDto.MonthlyBreakdown> buildMonthlyBreakdown(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        
        if (project.getStartDate() == null || project.getEndDate() == null) {
            return new ArrayList<>();
        }
        
        // Get all invoices for the project
        List<Invoice> invoices = invoiceRepository.findByProjectId(projectId);
        
        // Group invoices by month
        Map<String, BigDecimal> invoicesByMonth = invoices.stream()
                .collect(Collectors.groupingBy(
                    invoice -> invoice.getDueDate().getMonth().toString() + " " + invoice.getDueDate().getYear(),
                    Collectors.mapping(Invoice::getAmount,
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
        
        // Get all payments for the project
        List<Payment> payments = paymentRepository.findByInvoiceProjectId(projectId);
        
        // Group payments by month
        Map<String, BigDecimal> paymentsByMonth = payments.stream()
                .collect(Collectors.groupingBy(
                    payment -> payment.getPaymentDate().getMonth().toString() + " " + payment.getPaymentDate().getYear(),
                    Collectors.mapping(Payment::getAmount, 
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
                
        // Build the monthly breakdown list
        List<ProjectBudgetSummaryDto.MonthlyBreakdown> breakdowns = new ArrayList<>();
        
        // Start from project start date and go month by month until end date
        LocalDate currentDate = project.getStartDate().withDayOfMonth(1);
        LocalDate endMonthDate = project.getEndDate().withDayOfMonth(1);
        
        while (!currentDate.isAfter(endMonthDate)) {
            String monthKey = currentDate.getMonth().toString() + " " + currentDate.getYear();
            int year = currentDate.getYear();
            int monthValue = currentDate.getMonthValue();
            
            // Get values for this month
            BigDecimal invoicedAmount = invoicesByMonth.getOrDefault(monthKey, BigDecimal.ZERO);
            BigDecimal paidAmount = paymentsByMonth.getOrDefault(monthKey, BigDecimal.ZERO);
            Double hours = timeEntryRepository.calculateMonthlyHoursForProject(projectId, year, monthValue);
            
            BigDecimal totalValue = invoicedAmount;
            
            ProjectBudgetSummaryDto.MonthlyBreakdown breakdown = ProjectBudgetSummaryDto.MonthlyBreakdown.builder()
                    .month(monthKey)
                    .invoicedAmount(invoicedAmount)
                    .paidAmount(paidAmount)
                    .hours(hours)
                    .totalValue(totalValue)
                    .build();
                    
            breakdowns.add(breakdown);
            
            // Move to next month
            currentDate = currentDate.plusMonths(1);
        }
        
        return breakdowns;
    }
}