package com.freelancer.portal.dto;

import com.freelancer.portal.model.Payment;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for creating or updating a payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {
    
    /**
     * The ID of the payment (only used for updates).
     */
    private Long id;
    
    /**
     * The invoice this payment is for.
     */
    @NotNull(message = "Invoice ID is required")
    private Long invoiceId;
    
    /**
     * The amount being paid.
     */
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    /**
     * The method used for payment.
     */
    @NotBlank(message = "Payment method is required")
    @Size(max = 50, message = "Payment method cannot exceed 50 characters")
    private String paymentMethod;
    
    /**
     * The date when the payment was made.
     */
    private LocalDateTime paymentDate;
    
    /**
     * Transaction ID or reference number.
     */
    private String transactionId;
    
    /**
     * Additional notes about the payment.
     */
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
    
    /**
     * Status of the payment.
     */
    private Payment.Status status;
}