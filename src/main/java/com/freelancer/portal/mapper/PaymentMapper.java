package com.freelancer.portal.mapper;

import com.freelancer.portal.dto.PaymentDto;
import com.freelancer.portal.dto.PaymentRequestDto;
import com.freelancer.portal.dto.PaymentResponseDto;
import com.freelancer.portal.model.Invoice;
import com.freelancer.portal.model.Payment;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for mapping Payment entities to DTOs and vice versa.
 */
public class PaymentMapper {

    /**
     * Maps a Payment entity to a PaymentDto.
     *
     * @param payment the payment entity
     * @return the payment DTO
     */
    public static PaymentDto toDto(Payment payment) {
        if (payment == null) {
            return null;
        }

        return PaymentDto.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentDate(payment.getPaymentDate())
                .transactionId(payment.getTransactionId())
                .notes(payment.getNotes())
                .status(payment.getStatus())
                .invoiceId(payment.getInvoice().getId())
                .invoiceNumber(payment.getInvoice().getInvoiceNumber())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    /**
     * Maps a list of Payment entities to a list of PaymentDto objects.
     *
     * @param payments the list of payment entities
     * @return the list of payment DTOs
     */
    public static List<PaymentDto> toDtoList(List<Payment> payments) {
        if (payments == null) {
            return null;
        }
        
        return payments.stream()
                .map(PaymentMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Maps a PaymentRequestDto to a Payment entity.
     *
     * @param requestDto the payment request DTO
     * @param invoice the invoice entity this payment is for
     * @return the payment entity
     */
    public static Payment toEntity(PaymentRequestDto requestDto, Invoice invoice) {
        if (requestDto == null) {
            return null;
        }
        
        return Payment.builder()
                .invoice(invoice)
                .amount(requestDto.getAmount())
                .paymentMethod(requestDto.getPaymentMethod())
                .paymentDate(requestDto.getPaymentDate())
                .transactionId(requestDto.getTransactionId())
                .notes(requestDto.getNotes())
                .status(Payment.Status.COMPLETED)
                .build();
    }

    /**
     * Maps a Payment entity to a PaymentResponseDto.
     *
     * @param payment the payment entity
     * @return the payment response DTO
     */
    public static PaymentResponseDto toResponseDto(Payment payment) {
        if (payment == null) {
            return null;
        }
        
        // Handle null safely for company name
        String clientCompanyName = null;
        if (payment.getInvoice() != null && 
            payment.getInvoice().getClient() != null && 
            payment.getInvoice().getClient().getCompany() != null) {
            clientCompanyName = payment.getInvoice().getClient().getCompany().getName();
        }
        
        // Handle null safely for project name
        String projectName = null;
        if (payment.getInvoice() != null && 
            payment.getInvoice().getProject() != null) {
            projectName = payment.getInvoice().getProject().getName();
        }
        
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentDate(payment.getPaymentDate())
                .transactionId(payment.getTransactionId())
                .notes(payment.getNotes())
                .status(payment.getStatus())
                .invoiceId(payment.getInvoice().getId())
                .invoiceNumber(payment.getInvoice().getInvoiceNumber())
                .clientName(payment.getInvoice().getClient().getName())
                .clientCompanyName(clientCompanyName) // Use the safely retrieved value
                .projectName(projectName) // Use the safely retrieved value
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    /**
     * Maps a list of Payment entities to a list of PaymentResponseDto objects.
     *
     * @param payments the list of payment entities
     * @return the list of payment response DTOs
     */
    public static List<PaymentResponseDto> toResponseDtoList(List<Payment> payments) {
        if (payments == null) {
            return null;
        }
        
        return payments.stream()
                .map(PaymentMapper::toResponseDto)
                .collect(Collectors.toList());
    }
}