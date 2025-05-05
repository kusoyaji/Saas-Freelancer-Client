package com.freelancer.portal.repository;

import com.freelancer.portal.model.Invoice;
import com.freelancer.portal.model.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing InvoiceItem entities.
 */
@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {
    
    /**
     * Find all invoice items for a specific invoice.
     * 
     * @param invoice The invoice to find items for
     * @return A list of invoice items
     */
    List<InvoiceItem> findByInvoice(Invoice invoice);
    
    /**
     * Find all invoice items for a specific invoice ID.
     * 
     * @param invoiceId The ID of the invoice to find items for
     * @return A list of invoice items
     */
    List<InvoiceItem> findByInvoiceId(Long invoiceId);
    
    /**
     * Delete all invoice items for a specific invoice.
     * 
     * @param invoice The invoice to delete items for
     */
    void deleteByInvoice(Invoice invoice);
    
    /**
     * Delete all invoice items for a specific invoice ID.
     * 
     * @param invoiceId The ID of the invoice to delete items for
     */
    void deleteByInvoiceId(Long invoiceId);
}