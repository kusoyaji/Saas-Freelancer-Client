package com.freelancer.portal.dto;

import com.freelancer.portal.model.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponseDto {
    private Long id;
    private Long invoiceId;
    private String invoiceNumber;
    private String clientName;
    private String clientCompanyName;
    private BigDecimal amount;
    private String paymentMethod;
    private LocalDateTime paymentDate;
    private String projectName;
    private String transactionId;
    private String notes;
    private Payment.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}