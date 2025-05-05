package com.freelancer.portal.service;

import com.freelancer.portal.dto.PaymentRequestDto;
import com.freelancer.portal.dto.PaymentResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for payment operations.
 */
public interface PaymentService {

    /**
     * Get all payments for the current user with pagination.
     *
     * @param pageable pagination information
     * @return a page of payment response DTOs
     */
    Page<PaymentResponseDto> getAllPayments(Pageable pageable);

    /**
     * Get a payment by ID.
     *
     * @param id the payment ID
     * @return the payment response DTO
     */
    PaymentResponseDto getPaymentById(Long id);

    /**
     * Get payments for a specific invoice.
     *
     * @param invoiceId the invoice ID
     * @return a list of payment response DTOs
     */
    List<PaymentResponseDto> getPaymentsByInvoice(Long invoiceId);

    /**
     * Get payments for a date range.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return a list of payment response DTOs
     */
    List<PaymentResponseDto> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Create a new payment.
     *
     * @param paymentRequestDto the payment request DTO
     * @return the created payment response DTO
     */
    PaymentResponseDto createPayment(PaymentRequestDto paymentRequestDto);

    /**
     * Update an existing payment.
     *
     * @param id the payment ID
     * @param paymentRequestDto the payment request DTO
     * @return the updated payment response DTO
     */
    PaymentResponseDto updatePayment(Long id, PaymentRequestDto paymentRequestDto);

    /**
     * Delete a payment.
     *
     * @param id the payment ID
     */
    void deletePayment(Long id);

    /**
     * Check if the current user is the owner of the payment.
     *
     * @param id the payment ID
     * @return true if the current user is the owner, false otherwise
     */
    boolean isPaymentOwner(Long id);
}