package com.freelancer.portal.dto;

import com.freelancer.portal.model.Invoice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for invoice information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDto {

    private Long id;
    private String invoiceNumber;
    private Invoice.Status status;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private BigDecimal amount;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal taxRate;
    private BigDecimal discount;
    private BigDecimal amountPaid;
    private BigDecimal amountDue;
    private String description;
    private String paymentMethod;
    private String notes;
    private String currency;
    
    private Long clientId;
    private String clientName;
    
    private Long projectId;
    private String projectName;
    
    private Long freelancerId;
    private String freelancerName;
    
    private List<InvoiceItemDto> items;
    private List<PaymentDto> payments;
    
    private LocalDate sentDate;
    private LocalDate paidDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}