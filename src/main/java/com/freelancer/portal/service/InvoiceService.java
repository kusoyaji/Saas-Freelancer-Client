package com.freelancer.portal.service;

import com.freelancer.portal.dto.InvoiceDto;
import com.freelancer.portal.dto.InvoiceRequestDto;
import com.freelancer.portal.dto.InvoiceResponseDto;
import com.freelancer.portal.dto.PaymentResponseDto;
import com.freelancer.portal.model.FileMetadata;
import com.freelancer.portal.model.Invoice;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for invoice operations.
 */
public interface InvoiceService {

    /**
     * Get all invoices for the current user with pagination.
     *
     * @param pageable pagination information
     * @return a page of invoice response DTOs
     */
    Page<InvoiceResponseDto> getAllInvoices(Pageable pageable);

    /**
     * Get an invoice by ID.
     *
     * @param id the invoice ID
     * @return the invoice response DTO
     */
    InvoiceResponseDto getInvoiceById(Long id);

    /**
     * Get detailed information for an invoice by ID.
     *
     * @param id the invoice ID
     * @return the invoice detail DTO
     */
    InvoiceDto getInvoiceDetailById(Long id);

    /**
     * Get invoices for a specific client with pagination.
     *
     * @param clientId the client ID
     * @param pageable pagination information
     * @return a page of invoice response DTOs
     */
    Page<InvoiceResponseDto> getInvoicesByClient(Long clientId, Pageable pageable);

    /**
     * Get invoices by status with pagination.
     *
     * @param status the invoice status
     * @param pageable pagination information
     * @return a page of invoice response DTOs
     */
    Page<InvoiceResponseDto> getInvoicesByStatus(String status, Pageable pageable);

    /**
     * Get invoices for a date range.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return a list of invoice response DTOs
     */
    List<InvoiceResponseDto> getInvoicesByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Get invoices for a specific project with pagination.
     *
     * @param projectId the project ID
     * @param pageable pagination information
     * @return a page of invoice DTOs
     */
    Page<InvoiceDto> getInvoicesByProject(Long projectId, Pageable pageable);

    /**
     * Create a new invoice.
     *
     * @param invoiceRequestDto the invoice request DTO
     * @return the created invoice response DTO
     */
    InvoiceResponseDto createInvoice(InvoiceRequestDto invoiceRequestDto);

    /**
     * Update an existing invoice.
     *
     * @param id the invoice ID
     * @param invoiceRequestDto the invoice request DTO
     * @return the updated invoice response DTO
     */
    InvoiceResponseDto updateInvoice(Long id, InvoiceRequestDto invoiceRequestDto);

    /**
     * Update the status of an invoice.
     *
     * @param id the invoice ID
     * @param status the new status
     * @return the updated invoice DTO
     */
    InvoiceDto updateInvoiceStatus(Long id, Invoice.Status status);

    /**
     * Mark an invoice as paid.
     *
     * @param id the invoice ID
     * @return the updated invoice response DTO
     */
    InvoiceResponseDto markInvoiceAsPaid(Long id);

    /**
     * Mark an invoice as sent.
     *
     * @param id the invoice ID
     * @return the updated invoice response DTO
     */
    InvoiceResponseDto markInvoiceAsSent(Long id);

    /**
     * Delete an invoice.
     *
     * @param id the invoice ID
     */
    void deleteInvoice(Long id);

    /**
     * Get all overdue invoices.
     *
     * @return a list of invoice DTOs
     */
    List<InvoiceDto> getOverdueInvoices();

    /**
     * Check if the current user is the owner of the invoice.
     *
     * @param id the invoice ID
     * @return true if the current user is the owner, false otherwise
     */
    boolean isInvoiceOwner(Long id);
    
    /**
     * Generate a PDF for an invoice.
     *
     * @param invoiceId the invoice ID
     * @return the file metadata
     * @throws IOException if an I/O error occurs
     */
    FileMetadata generateInvoicePdf(Long invoiceId) throws IOException;
    
    /**
     * Download an invoice PDF.
     *
     * @param invoiceId the invoice ID
     * @return the file resource
     * @throws IOException if an I/O error occurs
     */
    Resource downloadInvoicePdf(Long invoiceId) throws IOException;

    /**
     * Get all payments for a specific invoice.
     *
     * @param invoiceId the invoice ID
     * @return a list of payment response DTOs
     */
    List<PaymentResponseDto> getInvoicePayments(Long invoiceId);
}