package com.freelancer.portal.service.impl;

import com.freelancer.portal.dto.PaymentRequestDto;
import com.freelancer.portal.dto.PaymentResponseDto;
import com.freelancer.portal.exception.ResourceNotFoundException;
import com.freelancer.portal.mapper.PaymentMapper;
import com.freelancer.portal.model.Invoice;
import com.freelancer.portal.model.Payment;
import com.freelancer.portal.model.User;
import com.freelancer.portal.repository.InvoiceRepository;
import com.freelancer.portal.repository.PaymentRepository;
import com.freelancer.portal.security.SecurityUtils;
import com.freelancer.portal.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponseDto> getAllPayments(Pageable pageable) {
        User currentUser = securityUtils.getCurrentUser();
        Page<Payment> payments = paymentRepository.findAllByInvoiceClientFreelancer(currentUser, pageable);
        return payments.map(PaymentMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        
        if (!isPaymentOwner(id)) {
            throw new ResourceNotFoundException("Payment not found with id: " + id);
        }
        
        return PaymentMapper.toResponseDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getPaymentsByInvoice(Long invoiceId) {
        User currentUser = securityUtils.getCurrentUser();
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));
        
        // Verify the invoice belongs to the current user
        if (!invoice.getClient().getFreelancer().equals(currentUser)) {
            throw new ResourceNotFoundException("Invoice not found with id: " + invoiceId);
        }
        
        List<Payment> payments = paymentRepository.findAllByInvoice(invoice);
        return PaymentMapper.toResponseDtoList(payments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        User currentUser = securityUtils.getCurrentUser();
        List<Payment> payments = paymentRepository.findAllByInvoiceClientFreelancerAndPaymentDateBetween(
                currentUser, startDate, endDate);
        return PaymentMapper.toResponseDtoList(payments);
    }

    @Override
    @Transactional
    public PaymentResponseDto createPayment(PaymentRequestDto requestDto) {
        User currentUser = securityUtils.getCurrentUser();
        Invoice invoice = invoiceRepository.findById(requestDto.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + requestDto.getInvoiceId()));
        
        // Verify the invoice belongs to the current user
        if (!invoice.getClient().getFreelancer().equals(currentUser)) {
            throw new ResourceNotFoundException("Invoice not found with id: " + requestDto.getInvoiceId());
        }
        
        // Check if the payment exceeds the amount due
        if (requestDto.getStatus() == Payment.Status.COMPLETED && 
            invoice.getAmountDue() != null && 
            requestDto.getAmount().compareTo(invoice.getAmountDue()) > 0) {
            throw new IllegalArgumentException("Payment amount exceeds the remaining amount due on this invoice");
        }
        
        // Remember the original status
        Invoice.Status originalStatus = invoice.getStatus();
        
        // Create payment using mapper
        Payment payment = PaymentMapper.toEntity(requestDto, invoice);
        
        // Set payment date if not provided
        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }
        
        // Add the payment to the invoice (this will trigger amount calculations and status updates)
        invoice.addPayment(payment);
        
        // Special handling for partial payments: 
        // If the amount is not fully paid and the status is not DRAFT or OVERDUE,
        // we should preserve the original SENT status
        if (payment.getStatus() == Payment.Status.COMPLETED && 
            invoice.getAmountDue().compareTo(BigDecimal.ZERO) > 0 &&
            (originalStatus == Invoice.Status.SENT || originalStatus == Invoice.Status.VIEWED)) {
            
            // For partial payments, preserve the original status
            invoice.setStatus(originalStatus);
        }
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // Save the invoice to persist payment method and status changes
        invoiceRepository.save(invoice);
        
        return PaymentMapper.toResponseDto(savedPayment);
    }

    @Override
    @Transactional
    public PaymentResponseDto updatePayment(Long id, PaymentRequestDto requestDto) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        
        if (!isPaymentOwner(id)) {
            throw new ResourceNotFoundException("Payment not found with id: " + id);
        }
        
        // If the invoice is being changed, verify it belongs to the current user
        Invoice currentInvoice = payment.getInvoice();
        Invoice targetInvoice = currentInvoice;
        
        if (!payment.getInvoice().getId().equals(requestDto.getInvoiceId())) {
            User currentUser = securityUtils.getCurrentUser();
            targetInvoice = invoiceRepository.findById(requestDto.getInvoiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + requestDto.getInvoiceId()));
            
            if (!targetInvoice.getClient().getFreelancer().equals(currentUser)) {
                throw new ResourceNotFoundException("Invoice not found with id: " + requestDto.getInvoiceId());
            }
        }
        
        // Check if updated payment amount would exceed the amount due on the target invoice
        if (requestDto.getStatus() == Payment.Status.COMPLETED) {
            // Calculate what the amount due would be after this update
            BigDecimal newAmountDue = targetInvoice.getAmountDue();
            
            // If same invoice but amount or status changed
            if (targetInvoice.getId().equals(currentInvoice.getId())) {
                // If previously COMPLETED, add back the old amount to available due amount
                if (payment.getStatus() == Payment.Status.COMPLETED) {
                    newAmountDue = newAmountDue.add(payment.getAmount());
                }
                
                // Check if new amount would exceed the adjusted amount due
                if (requestDto.getAmount().compareTo(newAmountDue) > 0) {
                    throw new IllegalArgumentException("Payment amount exceeds the remaining amount due on this invoice");
                }
            } 
            // Different invoice
            else {
                if (requestDto.getAmount().compareTo(targetInvoice.getAmountDue()) > 0) {
                    throw new IllegalArgumentException("Payment amount exceeds the amount due on the target invoice");
                }
            }
        }
        
        Payment.Status oldStatus = payment.getStatus();
        BigDecimal oldAmount = payment.getAmount();
        
        // If moving to a different invoice
        if (!currentInvoice.getId().equals(targetInvoice.getId())) {
            // Remove from old invoice
            currentInvoice.removePayment(payment);
            
            // Add to new invoice
            payment.setInvoice(targetInvoice);
            targetInvoice.addPayment(payment);
            
            // Save both invoices
            invoiceRepository.save(currentInvoice);
            invoiceRepository.save(targetInvoice);
        } else {
            // Update payment fields
            payment.setAmount(requestDto.getAmount());
            payment.setPaymentMethod(requestDto.getPaymentMethod());
            payment.setPaymentDate(requestDto.getPaymentDate());
            payment.setTransactionId(requestDto.getTransactionId());
            payment.setNotes(requestDto.getNotes());
            payment.setStatus(requestDto.getStatus());
            
            // Update payment method on invoice if it changed
            if (requestDto.getPaymentMethod() != null && 
                !requestDto.getPaymentMethod().equals(payment.getPaymentMethod())) {
                currentInvoice.setPaymentMethod(requestDto.getPaymentMethod());
            }
            
            // Handle status changes affecting invoice calculations
            boolean needsRecalculation = false;
            
            if (oldStatus != requestDto.getStatus() || !oldAmount.equals(requestDto.getAmount())) {
                needsRecalculation = true;
            }
            
            // Save payment
            Payment updatedPayment = paymentRepository.save(payment);
            
            // Update invoice if needed
           /* if (needsRecalculation) {
                currentInvoice.calculateAmounts();
                invoiceRepository.save(currentInvoice);
            }*/
            
            return PaymentMapper.toResponseDto(updatedPayment);
        }
        
        // Update payment fields
        payment.setAmount(requestDto.getAmount());
        payment.setPaymentMethod(requestDto.getPaymentMethod());
        payment.setPaymentDate(requestDto.getPaymentDate());
        payment.setTransactionId(requestDto.getTransactionId());
        payment.setNotes(requestDto.getNotes());
        payment.setStatus(requestDto.getStatus());
        
        Payment updatedPayment = paymentRepository.save(payment);
        return PaymentMapper.toResponseDto(updatedPayment);
    }

    @Override
    @Transactional
    public void deletePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        
        if (!isPaymentOwner(id)) {
            throw new ResourceNotFoundException("Payment not found with id: " + id);
        }
        
        // Cannot delete completed payments for PAID invoices
        Invoice invoice = payment.getInvoice();
        if (payment.getStatus() == Payment.Status.COMPLETED && 
            invoice.getStatus() == Invoice.Status.PAID) {
            throw new IllegalStateException("Cannot delete a completed payment for a paid invoice. Mark the invoice as unpaid first.");
        }
        
        // Remove from invoice
        invoice.removePayment(payment);
        
        // Update and save the invoice
        invoiceRepository.save(invoice);
        
        // Delete payment
        paymentRepository.delete(payment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isPaymentOwner(Long id) {
        User currentUser = securityUtils.getCurrentUser();
        Payment payment = paymentRepository.findById(id).orElse(null);
        
        if (payment == null) {
            return false;
        }
        
        return payment.getInvoice().getClient().getFreelancer().getId().equals(currentUser.getId());
    }
    
    // Removed mapToDto method as we now use PaymentMapper
}