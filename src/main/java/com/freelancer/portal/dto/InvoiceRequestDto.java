package com.freelancer.portal.dto;

import com.freelancer.portal.model.Invoice;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Data Transfer Object for creating or updating an invoice.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceRequestDto {
    
    /**
     * The ID of the invoice (only used for updates).
     */
    private Long id;
    
    /**
     * The project this invoice is associated with.
     */
    @NotNull(message = "Project ID is required")
    private Long projectId;
    
    /**
     * The client this invoice is associated with.
     */
    @NotNull(message = "Client ID is required")
    private Long clientId;
    
    /**
     * Optional invoice number (will be auto-generated if not provided).
     */
    @NotBlank(message = "Invoice number is required")
    private String invoiceNumber;
    
    /**
     * The status of the invoice.
     */
    private Invoice.Status status;
    
    /**
     * The date when this invoice is issued.
     */
    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;
    
    /**
     * The date by which payment is expected.
     */
    @NotNull(message = "Due date is required")
    private LocalDate dueDate;
    
    /**
     * Additional details about the services covered by this invoice.
     */
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    /**
     * The line items included in this invoice.
     */
    @Valid
    @NotEmpty(message = "At least one invoice item is required")
    private List<InvoiceItemRequestDto> items;
    
    /**
     * Additional notes for the invoice.
     */
    private String notes;
    
    /**
     * The tax rate applied to the invoice.
     */
    @NotNull(message = "Tax rate is required")
    @Positive(message = "Tax rate must be positive")
    private BigDecimal taxRate;
    
    /**
     * The tax amount for this invoice.
     */
    private BigDecimal taxAmount;
    private BigDecimal subTotal;
    
    /**
     * The discount applied to the invoice.
     */
    private BigDecimal discount;
    
    /**
     * The currency for the invoice.
     */
    private String currency;


}