package com.freelancer.portal.performance;

import com.freelancer.portal.model.*;
import com.freelancer.portal.repository.*;
import com.freelancer.portal.specification.InvoiceSpecification;
import com.freelancer.portal.specification.TimeEntrySpecification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Performance tests for database queries.
 * These tests measure the execution time of various queries
 * to help identify potential bottlenecks.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DatabasePerformanceTest {

    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private TimeEntryRepository timeEntryRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    private User testUser;
    private Project testProject;
    private Client testClient;
    
    @BeforeEach
    public void setUp() {
        // Note: In a real test scenario, we would set up test data,
        // but for this example, we'll assume data exists in the database
    }
    
    /**
     * Test invoice querying performance using specifications
     */
    @Test
    public void testInvoiceQueryPerformance() {
        // Measure execution time for invoice queries
        long startTime = System.nanoTime();
        
        // Complex query with multiple filters
        Specification<Invoice> spec = InvoiceSpecification.isOverdue()
            .and(InvoiceSpecification.amountGreaterThanOrEqual(new BigDecimal("100.00")));
            
        // Add pagination and sorting
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "issueDate"));
        Page<Invoice> invoices = invoiceRepository.findAll(spec, pageable);
        
        long endTime = System.nanoTime();
        long duration = TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);
        
        System.out.println("Invoice query execution time: " + duration + " ms");
        assertNotNull(invoices);
        
        // Test the optimized query directly
        startTime = System.nanoTime();
        List<Invoice> overdueInvoices = invoiceRepository.findAllOverdueInvoices();
        endTime = System.nanoTime();
        duration = TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);
        
        System.out.println("Optimized overdue query execution time: " + duration + " ms");
        assertNotNull(overdueInvoices);
    }
    
    /**
     * Test time entry querying performance with date filtering
     */
    @Test
    public void testTimeEntryQueryPerformance() {
        // Measure execution time for time entry queries
        long startTime = System.nanoTime();
        
        // Create a date range spanning one month
        LocalDate startDate = LocalDate.of(2025, Month.JANUARY, 1);
        LocalDate endDate = LocalDate.of(2025, Month.JANUARY, 31);
        
        // Build specification for date range + billable filter
        Specification<TimeEntry> spec = TimeEntrySpecification.dateRangeBetween(startDate, endDate)
            .and(TimeEntrySpecification.isBillable(true))
            .and(TimeEntrySpecification.isBilled(false));
            
        // Add pagination and sorting
        Pageable pageable = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "startTime"));
        Page<TimeEntry> timeEntries = timeEntryRepository.findAll(spec, pageable);
        
        long endTime = System.nanoTime();
        long duration = TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);
        
        System.out.println("Time entry query execution time: " + duration + " ms");
        assertNotNull(timeEntries);
    }
    
    /**
     * Test join query performance (payments associated with invoices for a project)
     */
    @Test
    public void testPaymentJoinQueryPerformance() {
        // Get a project ID (use any available one for testing)
        Project anyProject = projectRepository.findAll(PageRequest.of(0, 1)).getContent().get(0);
        
        long startTime = System.nanoTime();
        
        // Test payment calculation at database level
        BigDecimal totalAmount = paymentRepository.calculateTotalPaymentAmountByProjectId(anyProject.getId());
        
        long endTime = System.nanoTime();
        long duration = TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);
        
        System.out.println("Join query execution time: " + duration + " ms");
        assertNotNull(totalAmount);
    }
    
    /**
     * Test calculation query performance
     */
    @Test
    public void testCalculationQueryPerformance() {
        // Get a project ID (use any available one for testing)
        Project anyProject = projectRepository.findAll(PageRequest.of(0, 1)).getContent().get(0);
        
        long startTime = System.nanoTime();
        
        // Test optimized hours calculation
        Double totalHours = timeEntryRepository.calculateTotalHoursForProject(anyProject.getId());
        Double billableHours = timeEntryRepository.calculateBillableHoursForProject(anyProject.getId());
        Double billedHours = timeEntryRepository.calculateBilledHoursForProject(anyProject.getId());
        Double unbilledHours = timeEntryRepository.calculateUnbilledHoursForProject(anyProject.getId());
        
        long endTime = System.nanoTime();
        long duration = TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);
        
        System.out.println("Calculation query execution time: " + duration + " ms");
        System.out.println("Total hours: " + totalHours);
        System.out.println("Billable hours: " + billableHours);
        System.out.println("Billed hours: " + billedHours);
        System.out.println("Unbilled hours: " + unbilledHours);
        
        assertNotNull(totalHours);
    }
    
    /**
     * Test monthly calculation performance
     */
    @Test
    public void testMonthlyCalculationPerformance() {
        // Get a project ID (use any available one for testing)
        Project anyProject = projectRepository.findAll(PageRequest.of(0, 1)).getContent().get(0);
        
        long startTime = System.nanoTime();
        
        // Test monthly hours calculation for the current year
        int currentYear = LocalDate.now().getYear();
        
        // Calculate hours for each month
        for (int month = 1; month <= 12; month++) {
            Double monthlyHours = timeEntryRepository.calculateMonthlyHoursForProject(
                anyProject.getId(), currentYear, month);
            System.out.printf("Month %d: %.2f hours%n", month, monthlyHours);
        }
        
        long endTime = System.nanoTime();
        long duration = TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);
        
        System.out.println("Monthly calculation execution time: " + duration + " ms");
    }
}