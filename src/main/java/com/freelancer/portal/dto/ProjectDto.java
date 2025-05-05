package com.freelancer.portal.dto;

import com.freelancer.portal.model.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for project summary information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {

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
    
    private Integer filesCount;
    private Integer invoicesCount;
    private Integer messagesCount;
    private Integer conversationsCount;
    private Integer unreadMessagesCount;
    private BigDecimal invoicedAmount;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
}