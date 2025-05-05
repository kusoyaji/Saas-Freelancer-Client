package com.freelancer.portal.controller;

import com.freelancer.portal.dto.PaymentRequestDto;
import com.freelancer.portal.dto.PaymentResponseDto;
import com.freelancer.portal.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for payment operations.
 */
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    
    /**
     * Get all payments for the current user with pagination.
     */
    @GetMapping
    public ResponseEntity<Page<PaymentResponseDto>> getAllPayments(Pageable pageable) {
        return ResponseEntity.ok(paymentService.getAllPayments(pageable));
    }
    
    /**
     * Get a payment by ID.
     * Only accessible if the payment belongs to the current user.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<PaymentResponseDto> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }
    
    /**
     * Get payments for a specific invoice with pagination.
     * Only accessible if the invoice belongs to the current user.
     */
    @GetMapping("/invoice/{invoiceId}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByInvoice(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(paymentService.getPaymentsByInvoice(invoiceId));
    }
    
    /**
     * Get payments for a date range.
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(paymentService.getPaymentsByDateRange(startDate, endDate));
    }
    
    /**
     * Create a new payment.
     */
    @PostMapping
    public ResponseEntity<PaymentResponseDto> createPayment(@Valid @RequestBody PaymentRequestDto paymentRequestDto) {
        return new ResponseEntity<>(paymentService.createPayment(paymentRequestDto), HttpStatus.CREATED);
    }
    
    /**
     * Update an existing payment.
     * Only accessible if the payment belongs to the current user.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<PaymentResponseDto> updatePayment(
            @PathVariable Long id, @Valid @RequestBody PaymentRequestDto paymentRequestDto) {
        return ResponseEntity.ok(paymentService.updatePayment(id, paymentRequestDto));
    }
    
    /**
     * Delete a payment.
     * Only accessible if the payment belongs to the current user.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}