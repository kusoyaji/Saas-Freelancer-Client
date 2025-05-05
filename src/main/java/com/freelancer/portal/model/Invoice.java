package com.freelancer.portal.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an invoice in the freelancing platform.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Invoice number is required")
    @Size(max = 50, message = "Invoice number cannot exceed 50 characters")
    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @NotNull(message = "Issue date is required")
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @NotNull(message = "Due date is required")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0", message = "Amount must be a positive number")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Subtotal is required")
    @DecimalMin(value = "0", message = "Subtotal must be a positive number")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    @DecimalMin(value = "0", message = "Tax amount must be a positive number")
    @Column(name = "tax_amount", precision = 19, scale = 2)
    private BigDecimal taxAmount;

    @DecimalMin(value = "0", message = "Tax rate must be a positive number")
    @Column(name = "tax_rate", precision = 10, scale = 2)
    private BigDecimal taxRate;

    @DecimalMin(value = "0", message = "Discount must be a positive number")
    @Column(precision = 19, scale = 2)
    private BigDecimal discount;

    @Column(name = "amount_paid", precision = 19, scale = 2)
    private BigDecimal amountPaid;

    @Column(name = "amount_due", precision = 19, scale = 2)
    private BigDecimal amountDue;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Size(max = 50, message = "Payment method cannot exceed 50 characters")
    @Column(name = "payment_method")
    private String paymentMethod;

    @Size(max = 2000, message = "Notes cannot exceed 2000 characters")
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    @Column(length = 3)
    private String currency;

    @NotNull(message = "Client is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonBackReference
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @JsonBackReference("project-invoices")
    private Project project;

    @NotNull(message = "Freelancer is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freelancer_id", nullable = false)
    private User freelancer;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("invoice-items")
    @Builder.Default
    private List<InvoiceItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @Column(name = "sent_date")
    private LocalDate sentDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (status == null) {
            status = Status.DRAFT;
        }
        calculateAmounts();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateAmounts();
    }

    private void calculateAmounts() {
        // Initialize values to prevent null pointer exceptions
        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (taxAmount == null) taxAmount = BigDecimal.ZERO;
        if (amountPaid == null) amountPaid = BigDecimal.ZERO;
        if (amountDue == null) amountDue = BigDecimal.ZERO;
        if (discount == null) discount = BigDecimal.ZERO;
        if (amount == null) amount = BigDecimal.ZERO;
        
        // Calculate subtotal from items if empty
        if (subtotal.compareTo(BigDecimal.ZERO) == 0 && items != null && !items.isEmpty()) {
            subtotal = items.stream()
                    .filter(item -> item.getAmount() != null)
                    .map(InvoiceItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // Calculate tax amount if tax rate is set
        if (taxRate != null && subtotal != null) {
            try {
                taxAmount = subtotal.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            } catch (ArithmeticException e) {
                // Handle division errors
                taxAmount = BigDecimal.ZERO;
            }
        }

        // Calculate total amount
        amount = subtotal.add(taxAmount).subtract(discount);
        
        // Calculate amount paid from COMPLETED payments only
        amountPaid = BigDecimal.ZERO; // Reset to avoid double counting
        if (payments != null && !payments.isEmpty()) {
            amountPaid = payments.stream()
                    .filter(p -> p != null && p.getStatus() == Payment.Status.COMPLETED)
                    .filter(p -> p.getAmount() != null)
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Calculate amount due (never allow negative)
            amountDue = amount.subtract(amountPaid);
            if (amountDue.compareTo(BigDecimal.ZERO) < 0) {
                amountDue = BigDecimal.ZERO;
            }

            // Update status based on payments - but don't create a recursive loop
            if (!isCalculatingStatus) {
                updateStatusBasedOnPayments();
            }
        } else {
            amountDue = amount;
        }
    }

    /**
     * This flag prevents recursion between calculateAmounts and updateStatusBasedOnPayments
     */
    @Transient
    @Builder.Default
    private boolean isCalculatingStatus = false;

    /**
     * Updates the invoice status based on the payments.
     * If amount due is zero or negative, status is set to PAID.
     * Otherwise, preserves DRAFT, SENT, VIEWED or OVERDUE status for unpaid/partially paid invoices.
     */
    public void updateStatusBasedOnPayments() {
        // Set flag to prevent recursive calls
        isCalculatingStatus = true;
        
        try {
            // If no payment has been made, don't change status
            if (amountPaid == null || amountPaid.compareTo(BigDecimal.ZERO) == 0) {
                // Keep current status if it's DRAFT, SENT, VIEWED or OVERDUE
                if (status == Status.PAID) {
                    // Reset to SENT if it was previously paid
                    status = Status.SENT;
                    paidDate = null; // Reset paid date
                }
                return;
            }
            
            // If fully paid (amount_due is 0), set to PAID
            if (amountDue != null && amountDue.compareTo(BigDecimal.ZERO) <= 0) {
                status = Status.PAID;
                // Only set paid date if it hasn't been set before
                if (paidDate == null) {
                    paidDate = LocalDate.now();
                }
            } 
            // For partial payments: check for overdue but otherwise maintain current status
            else if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
                // Only update status to OVERDUE if the due date is passed and it's not already OVERDUE
                if (status != Status.OVERDUE && dueDate != null && LocalDate.now().isAfter(dueDate)) {
                    status = Status.OVERDUE;
                }
                // Otherwise, maintain the current status (SENT, VIEWED, etc.) - don't change to PARTIALLY_PAID
                
                // If it was previously marked as paid, reset the paid date
                if (paidDate != null) {
                    paidDate = null;
                }
            }
        } finally {
            // Reset flag after we're done
            isCalculatingStatus = false;
        }
    }

    /**
     * Add an item to the invoice.
     *
     * @param item the item to add
     */
    public void addItem(InvoiceItem item) {
        items.add(item);
        item.setInvoice(this);
        calculateAmounts();
    }

    /**
     * Remove an item from the invoice.
     *
     * @param item the item to remove
     */
    public void removeItem(InvoiceItem item) {
        items.remove(item);
        item.setInvoice(null);
        calculateAmounts();
    }

    /**
     * Add a payment to the invoice.
     *
     * @param payment the payment to add
     */
    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setInvoice(this);
        
        // Update invoice payment method if not set and payment has a method
        if ((paymentMethod == null || paymentMethod.isEmpty()) && 
            payment.getPaymentMethod() != null && !payment.getPaymentMethod().isEmpty()) {
            this.paymentMethod = payment.getPaymentMethod();
        }
        
        calculateAmounts();
    }

    /**
     * Remove a payment from the invoice.
     *
     * @param payment the payment to remove
     */
    public void removePayment(Payment payment) {
        payments.remove(payment);
        payment.setInvoice(null);
        calculateAmounts();
    }

    /**
     * Invoice status enumeration.
     */
    public enum Status {
        DRAFT, SENT, VIEWED, OVERDUE, PAID, PARTIALLY_PAID, CANCELLED
    }
}