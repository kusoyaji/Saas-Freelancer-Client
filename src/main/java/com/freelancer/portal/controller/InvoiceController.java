package com.freelancer.portal.controller;

import com.freelancer.portal.dto.InvoiceDto;
import com.freelancer.portal.dto.InvoiceRequestDto;
import com.freelancer.portal.dto.InvoiceResponseDto;
import com.freelancer.portal.dto.PaymentResponseDto;
import com.freelancer.portal.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for invoice operations.
 * The base path /api/v1 is configured in application.yml via server.servlet.context-path
 */
@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private static final Logger log = LoggerFactory.getLogger(InvoiceController.class);
    
    /**
     * Get all invoices for the current user with pagination.
     */
    @GetMapping
    public ResponseEntity<Page<InvoiceResponseDto>> getAllInvoices(Pageable pageable) {
        return ResponseEntity.ok(invoiceService.getAllInvoices(pageable));
    }
    
    /**
     * Get an invoice by ID.
     * Only accessible if the invoice belongs to the current user.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<InvoiceResponseDto> getInvoiceById(@PathVariable Long id) {
        try {
            log.debug("Fetching invoice with ID: {}", id);
            InvoiceResponseDto invoice = invoiceService.getInvoiceById(id);
            return ResponseEntity.ok(invoice);
        } catch (Exception e) {
            log.error("Error fetching invoice with ID: {}", id, e);
            throw e;
        }
    }
    
    /**
     * Get invoices for a specific client with pagination.
     * Only accessible if the client belongs to the current user.
     */
    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<Page<InvoiceResponseDto>> getInvoicesByClient(
            @PathVariable Long clientId, Pageable pageable) {
        return ResponseEntity.ok(invoiceService.getInvoicesByClient(clientId, pageable));
    }
    
    /**
     * Get invoices by status with pagination.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<InvoiceResponseDto>> getInvoicesByStatus(
            @PathVariable String status, Pageable pageable) {
        return ResponseEntity.ok(invoiceService.getInvoicesByStatus(status, pageable));
    }
    
    /**
     * Get invoices for a date range.
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<InvoiceResponseDto>> getInvoicesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(invoiceService.getInvoicesByDateRange(startDate, endDate));
    }
    
    /**
     * Create a new invoice.
     */
    @PostMapping
    public ResponseEntity<InvoiceResponseDto> createInvoice(@Valid @RequestBody InvoiceRequestDto invoiceRequestDto) {
        return new ResponseEntity<>(invoiceService.createInvoice(invoiceRequestDto), HttpStatus.CREATED);
    }
    
    /**
     * Update an existing invoice.
     * Only accessible if the invoice belongs to the current user.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<InvoiceResponseDto> updateInvoice(
            @PathVariable Long id, @Valid @RequestBody InvoiceRequestDto invoiceRequestDto) {
        return ResponseEntity.ok(invoiceService.updateInvoice(id, invoiceRequestDto));
    }
    
    /**
     * Mark an invoice as paid.
     * Only accessible if the invoice belongs to the current user.
     */
    @PatchMapping("/{id}/mark-paid")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<InvoiceResponseDto> markInvoiceAsPaid(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.markInvoiceAsPaid(id));
    }
    
    /**
     * Mark an invoice as sent.
     * Only accessible if the invoice belongs to the current user.
     */
    @PatchMapping("/{id}/mark-sent")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<InvoiceResponseDto> markInvoiceAsSent(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.markInvoiceAsSent(id));
    }
    
    /**
     * Get all payments for a specific invoice.
     * Only accessible if the invoice belongs to the current user.
     */
    @GetMapping("/{id}/payments")
    @PreAuthorize("@invoiceService.isInvoiceOwner(#id)")
    public ResponseEntity<List<PaymentResponseDto>> getInvoicePayments(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoicePayments(id));
    }
    
    /**
     * Get invoices for a specific project with pagination.
     * Only accessible if the project belongs to the current user.
     */
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<Page<InvoiceDto>> getInvoicesByProject(
            @PathVariable Long projectId, Pageable pageable) {
        log.debug("Fetching invoices for project ID: {}", projectId);
        return ResponseEntity.ok(invoiceService.getInvoicesByProject(projectId, pageable));
    }
    
    /**
     * Download invoice as PDF.
     * Only accessible if the invoice belongs to the current user.
     */
    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<org.springframework.core.io.Resource> downloadInvoicePdf(@PathVariable Long id) {
        try {
            log.debug("Downloading PDF for invoice with ID: {}", id);
            org.springframework.core.io.Resource pdfResource = invoiceService.downloadInvoicePdf(id);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=invoice_" + id + ".pdf")
                    .header("Content-Type", "application/pdf")
                    .body(pdfResource);
        } catch (Exception e) {
            log.error("Error downloading PDF for invoice with ID: {}", id, e);
            throw new com.freelancer.portal.exception.ResourceNotFoundException("PDF for invoice with ID " + id + " could not be generated");
        }
    }
    
    /**
     * Delete an invoice.
     * Only accessible if the invoice belongs to the current user.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }
}