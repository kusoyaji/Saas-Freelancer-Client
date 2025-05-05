package com.freelancer.portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for creating or updating an invoice item.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemRequestDto {
    
    /**
     * The ID of the invoice item (only used for updates).
     */
    private Long id;
    
    /**
     * Description of the service or product being billed.
     */
    @NotBlank(message = "Description is required")
    private String description;
    
    /**
     * The quantity of the item being billed.
     */
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private BigDecimal quantity;
    
    /**
     * The unit price of the item.
     */
    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    private BigDecimal unitPrice;
    
    /**
     * The project this item is associated with (optional).
     */
    private Long projectId;
}