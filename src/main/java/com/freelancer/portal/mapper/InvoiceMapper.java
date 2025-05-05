package com.freelancer.portal.mapper;

import com.freelancer.portal.dto.InvoiceDto;
import com.freelancer.portal.dto.InvoiceItemDto;
import com.freelancer.portal.dto.PaymentDto;
import com.freelancer.portal.model.Invoice;
import com.freelancer.portal.model.InvoiceItem;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for mapping Invoice entities to DTOs and vice versa.
 */
public class InvoiceMapper {

    /**
     * Maps an Invoice entity to an InvoiceDto.
     *
     * @param invoice the invoice entity
     * @return the invoice DTO
     */
    public static InvoiceDto toDto(Invoice invoice) {
        if (invoice == null) {
            return null;
        }

        var builder = InvoiceDto.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .amount(invoice.getAmount())
                .discount(invoice.getDiscount())
                .description(invoice.getDescription())
                .paymentMethod(invoice.getPaymentMethod())
                .currency(invoice.getCurrency())
                .subtotal(invoice.getSubtotal())
                .taxAmount(invoice.getTaxAmount())
                .taxRate(invoice.getTaxRate())
                .amountPaid(invoice.getAmountPaid())
                .amountDue(invoice.getAmountDue())
                .status(invoice.getStatus())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .paidDate(invoice.getPaidDate())
                .sentDate(invoice.getSentDate())
                .notes(invoice.getNotes())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt());
        
        // Safely handle client if present
        if (invoice.getClient() != null) {
            builder.clientId(invoice.getClient().getId())
                  .clientName(invoice.getClient().getName());
        }
        
        // Safely handle project if present
        if (invoice.getProject() != null) {
            builder.projectId(invoice.getProject().getId())
                  .projectName(invoice.getProject().getName());
        }
        
        // Safely handle freelancer if present
        if (invoice.getFreelancer() != null) {
            String firstName = invoice.getFreelancer().getFirstName() != null ? invoice.getFreelancer().getFirstName() : "";
            String lastName = invoice.getFreelancer().getLastName() != null ? invoice.getFreelancer().getLastName() : "";
            
            // Improved logic for constructing fullName
            StringBuilder fullNameBuilder = new StringBuilder();
            if (!firstName.isEmpty()) {
                fullNameBuilder.append(firstName);
            }
            if (!lastName.isEmpty()) {
                if (fullNameBuilder.length() > 0) {
                    fullNameBuilder.append(" ");
                }
                fullNameBuilder.append(lastName);
            }
            
            String fullName = fullNameBuilder.toString();
            // If both names are empty, use a default value
            if (fullName.isEmpty()) {
                fullName = "Unknown";
            }
            
            builder.freelancerId(invoice.getFreelancer().getId())
                  .freelancerName(fullName);
        }
        
        InvoiceDto dto = builder.build();

        // Map invoice items if present
        if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
            List<InvoiceItemDto> itemDtos = invoice.getItems().stream()
                    .map(InvoiceMapper::mapInvoiceItemToDto)
                    .filter(item -> item != null)
                    .collect(Collectors.toList());
            dto.setItems(itemDtos);
        }

        // Map payments if present
        if (invoice.getPayments() != null && !invoice.getPayments().isEmpty()) {
            List<PaymentDto> paymentDtos = invoice.getPayments().stream()
                    .map(payment -> PaymentMapper.toDto(payment))
                    .filter(payment -> payment != null)
                    .collect(Collectors.toList());
            dto.setPayments(paymentDtos);
        }

        return dto;
    }

    /**
     * Maps an InvoiceItem entity to an InvoiceItemDto.
     *
     * @param item the invoice item entity
     * @return the invoice item DTO
     */
    private static InvoiceItemDto mapInvoiceItemToDto(InvoiceItem item) {
        if (item == null) {
            return null;
        }
        
        return InvoiceItemDto.builder()
                .id(item.getId())
                .description(item.getDescription())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .amount(item.getAmount())
                .build();
    }
}