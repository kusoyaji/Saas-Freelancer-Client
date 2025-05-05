package com.freelancer.portal.controller.monitoring;

import com.freelancer.portal.dto.PaginatedResponse;
import com.freelancer.portal.model.Invoice;
import com.freelancer.portal.model.TimeEntry;
import com.freelancer.portal.repository.InvoiceRepository;
import com.freelancer.portal.repository.TimeEntryRepository;
import com.freelancer.portal.specification.InvoiceSpecification;
import com.freelancer.portal.specification.TimeEntrySpecification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Controller for database performance testing.
 * This controller is only active in non-production environments.
 */
@RestController
@RequestMapping("/monitoring/performance")
@Profile("!prod") // Only active in non-production environments
public class PerformanceTestController {

    private static final Logger log = LoggerFactory.getLogger(PerformanceTestController.class);

    private final InvoiceRepository invoiceRepository;
    private final TimeEntryRepository timeEntryRepository;

    @Autowired
    public PerformanceTestController(InvoiceRepository invoiceRepository, 
                                    TimeEntryRepository timeEntryRepository) {
        this.invoiceRepository = invoiceRepository;
        this.timeEntryRepository = timeEntryRepository;
    }

    /**
     * Test invoice query performance with various filters
     */
    @GetMapping("/invoices")
    public ResponseEntity<Map<String, Object>> testInvoiceQueryPerformance(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        
        log.info("Testing invoice query performance");
        Map<String, Object> results = new HashMap<>();
        
        // Build a specification with the provided filters
        Specification<Invoice> spec = Specification.where(null);
        
        if (status != null && !status.isEmpty()) {
            try {
                Invoice.Status statusEnum = Invoice.Status.valueOf(status.toUpperCase());
                spec = spec.and(InvoiceSpecification.hasStatus(statusEnum));
                results.put("filter.status", statusEnum);
            } catch (IllegalArgumentException e) {
                results.put("filter.status.error", "Invalid status: " + status);
            }
        }
        
        if (minAmount != null) {
            spec = spec.and(InvoiceSpecification.amountGreaterThanOrEqual(minAmount));
            results.put("filter.minAmount", minAmount);
        }
        
        if (startDate != null && endDate != null) {
            spec = spec.and(InvoiceSpecification.issueDateBetween(startDate, endDate));
            results.put("filter.dateRange", startDate + " to " + endDate);
        }
        
        // Execute query with timing
        long startTime = System.nanoTime();
        
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "issueDate"));
        Page<Invoice> invoices = invoiceRepository.findAll(spec, pageable);
        
        long endTime = System.nanoTime();
        long duration = TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);
        
        // Collect results
        results.put("query.type", "Invoice filtering");
        results.put("query.executionTime", duration + " ms");
        results.put("query.resultCount", invoices.getTotalElements());
        results.put("query.pageCount", invoices.getTotalPages());
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * Test time entry query performance with various filters
     */
    @GetMapping("/time-entries")
    public ResponseEntity<Map<String, Object>> testTimeEntryQueryPerformance(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Boolean billable,
            @RequestParam(required = false) Boolean billed,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
            
        log.info("Testing time entry query performance");
        Map<String, Object> results = new HashMap<>();
        
        // Build a specification with the provided filters
        Specification<TimeEntry> spec = Specification.where(null);
        
        if (projectId != null) {
            spec = spec.and(TimeEntrySpecification.hasProjectId(projectId));
            results.put("filter.projectId", projectId);
        }
        
        if (billable != null) {
            spec = spec.and(TimeEntrySpecification.isBillable(billable));
            results.put("filter.billable", billable);
        }
        
        if (billed != null) {
            spec = spec.and(TimeEntrySpecification.isBilled(billed));
            results.put("filter.billed", billed);
        }
        
        if (startDate != null && endDate != null) {
            spec = spec.and(TimeEntrySpecification.dateRangeBetween(startDate, endDate));
            results.put("filter.dateRange", startDate + " to " + endDate);
        }
        
        // Execute query with timing
        long startTime = System.nanoTime();
        
        Pageable pageable = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "startTime"));
        Page<TimeEntry> timeEntries = timeEntryRepository.findAll(spec, pageable);
        
        long endTime = System.nanoTime();
        long duration = TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);
        
        // Collect results
        results.put("query.type", "TimeEntry filtering");
        results.put("query.executionTime", duration + " ms");
        results.put("query.resultCount", timeEntries.getTotalElements());
        results.put("query.pageCount", timeEntries.getTotalPages());
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * Test optimized vs. non-optimized query performance
     */
    @GetMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareQueryPerformance() {
        log.info("Comparing optimized vs non-optimized queries");
        Map<String, Object> results = new HashMap<>();
        
        // Test standard query approach
        long startTime1 = System.nanoTime();
        Page<Invoice> overdueInvoicesSpec = invoiceRepository.findAll(
            InvoiceSpecification.isOverdue(), 
            PageRequest.of(0, 20)
        );
        long endTime1 = System.nanoTime();
        long duration1 = TimeUnit.MILLISECONDS.convert(endTime1 - startTime1, TimeUnit.NANOSECONDS);
        
        // Test optimized query approach
        long startTime2 = System.nanoTime();
        var overdueInvoicesOptimized = invoiceRepository.findAllOverdueInvoices();
        long endTime2 = System.nanoTime();
        long duration2 = TimeUnit.MILLISECONDS.convert(endTime2 - startTime2, TimeUnit.NANOSECONDS);
        
        // Collect results
        results.put("standard.query.executionTime", duration1 + " ms");
        results.put("standard.query.resultCount", overdueInvoicesSpec.getTotalElements());
        
        results.put("optimized.query.executionTime", duration2 + " ms");
        results.put("optimized.query.resultCount", overdueInvoicesOptimized.size());
        
        results.put("improvement", 
            duration1 > 0 ? 
            String.format("%.2f%%", (1 - (double)duration2 / duration1) * 100) : 
            "N/A");
            
        return ResponseEntity.ok(results);
    }
}