package com.freelancer.portal.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a payment made against an invoice.
 * <p>
 * Payments track individual transactions made toward satisfying an invoice.
 * This allows for partial payments and multiple payment methods for a single invoice.
 * Each payment records the amount, method, and transaction details.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The invoice this payment is applied to.
     */
    @NotNull(message = "Invoice is required")
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    /**
     * The amount of this payment.
     */
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /**
     * The method used for this payment (e.g., Credit Card, Bank Transfer).
     */
    @Size(max = 50, message = "Payment method cannot exceed 50 characters")
    @Column(name = "payment_method")
    private String paymentMethod;

    /**
     * The date when this payment was made.
     */
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    /**
     * Transaction ID or reference number from the payment processor.
     */
    @Size(max = 100, message = "Transaction ID cannot exceed 100 characters")
    @Column(name = "transaction_id")
    private String transactionId;

    /**
     * Additional notes about this payment.
     */
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * The current status of this payment.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    /**
     * Timestamp when the payment record was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the payment record was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (paymentDate == null) {
            paymentDate = LocalDateTime.now();
        }
        if (status == null) {
            status = Status.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Possible statuses for a payment.
     * Synchronized with frontend PaymentStatus enum for consistency.
     */
    public enum Status {
        PENDING, COMPLETED, FAILED, REFUNDED, CANCELLED
    }
}