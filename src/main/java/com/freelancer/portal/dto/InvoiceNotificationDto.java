package com.freelancer.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for invoice notification information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceNotificationDto {
    
    private Long invoiceId;
    private String invoiceNumber;
    private String clientName;
    private String clientEmail;
    private BigDecimal amount;
    private LocalDate dueDate;
    private Long daysOverdue;
    private String projectName;
}