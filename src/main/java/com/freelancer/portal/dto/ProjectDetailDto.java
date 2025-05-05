package com.freelancer.portal.dto;

import com.freelancer.portal.model.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for detailed project information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDetailDto {

    private Long id;
    private String name;
    private String description;
    private Project.Status status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime completedAt;
    private BigDecimal budget;
    private BigDecimal hourlyRate;
    
    private Long clientId;
    private String clientName;
    private String clientEmail;
    private String clientPhone;
    
    private Long freelancerId;
    private String freelancerName;
    private String freelancerEmail;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private List<FileResponseDto> files;
    private List<InvoiceDto> invoices;
    private List<MessageDto> messages;
    
    private BigDecimal invoicedAmount;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    
    // Total count of unread messages for this project
    private Long unreadMessagesCount;
}