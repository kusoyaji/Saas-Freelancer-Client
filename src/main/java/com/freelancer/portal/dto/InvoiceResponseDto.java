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

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceResponseDto {
    private Long id;
    private String invoiceNumber;
    private Long clientId;
    private String clientName;
    private String clientCompanyName;
    private Long projectId;
    private String projectName;
    private Invoice.Status status;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private List<InvoiceItemResponseDto> items;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal taxRate;
    private BigDecimal discount;
    private BigDecimal total;
    private BigDecimal amountPaid;
    private BigDecimal amountDue;
    private String notes;
    private String currency;
    private LocalDate sentDate;
    private LocalDate paidDate;
    private Boolean isOverdue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}