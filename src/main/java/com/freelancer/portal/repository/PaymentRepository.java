package com.freelancer.portal.repository;

import com.freelancer.portal.model.Invoice;
import com.freelancer.portal.model.Payment;
import com.freelancer.portal.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for managing Payment entities with optimized queries.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {
    
    /**
     * Find all payments for a specific invoice with eager loading.
     * 
     * @param invoice The invoice to find payments for
     * @return A list of payments
     */
    @EntityGraph(attributePaths = {"paymentMethod"})
    List<Payment> findByInvoice(Invoice invoice);
    
    /**
     * Find all payments for a specific invoice with eager loading.
     * 
     * @param invoice The invoice to find payments for
     * @return A list of payments
     */
    @EntityGraph(attributePaths = {"paymentMethod"})
    List<Payment> findAllByInvoice(Invoice invoice);
    
    /**
     * Find all payments for a specific invoice ID with pagination and eager loading.
     * 
     * @param invoiceId The ID of the invoice to find payments for
     * @param pageable Pagination information
     * @return A page of payments
     */
    @EntityGraph(attributePaths = {"invoice", "paymentMethod"})
    Page<Payment> findByInvoiceId(Long invoiceId, Pageable pageable);
    
    /**
     * Find all payments for a specific invoice ID with eager loading.
     * 
     * @param invoiceId The ID of the invoice to find payments for
     * @return A list of payments
     */
    @EntityGraph(attributePaths = {"invoice", "paymentMethod"})
    List<Payment> findByInvoiceId(Long invoiceId);
    
    /**
     * Find all payments with a specific status with eager loading.
     * 
     * @param status The status to filter by
     * @return A list of payments
     */
    @EntityGraph(attributePaths = {"invoice"})
    List<Payment> findByStatus(Payment.Status status);
    
    /**
     * Find all payments made after a specific date with eager loading.
     * 
     * @param date The date to filter by
     * @return A list of payments
     */
    @EntityGraph(attributePaths = {"invoice"})
    List<Payment> findByPaymentDateAfter(LocalDateTime date);
    
    /**
     * Find all payments made between two dates with eager loading.
     * 
     * @param startDate The start date
     * @param endDate The end date
     * @return A list of payments
     */
    @EntityGraph(attributePaths = {"invoice"})
    List<Payment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find all payments for invoices associated with a freelancer with optimized join.
     *
     * @param freelancer The freelancer user
     * @param pageable Pagination information
     * @return A page of payments
     */
    @Query("SELECT p FROM Payment p JOIN FETCH p.invoice i JOIN i.client c WHERE c.freelancer = :freelancer")
    Page<Payment> findAllByInvoiceClientFreelancer(@Param("freelancer") User freelancer, Pageable pageable);
    
    /**
     * Find all payments for invoices associated with a freelancer within a date range with optimized join.
     *
     * @param freelancer The freelancer user
     * @param startDate The start date
     * @param endDate The end date
     * @return A list of payments
     */
    @Query("SELECT p FROM Payment p JOIN FETCH p.invoice i JOIN i.client c " +
           "WHERE c.freelancer = :freelancer AND p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findAllByInvoiceClientFreelancerAndPaymentDateBetween(
            @Param("freelancer") User freelancer,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find all payments for invoices associated with a specific project with eager loading.
     * 
     * @param projectId The project ID
     * @return A list of payments
     */
    @EntityGraph(attributePaths = {"invoice"})
    List<Payment> findByInvoiceProjectId(Long projectId);
    
    /**
     * Calculate total payment amount for a project.
     * Optimized to perform calculation at database level.
     * 
     * @param projectId The project ID
     * @return The total payment amount
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p JOIN p.invoice i WHERE i.project.id = :projectId")
    BigDecimal calculateTotalPaymentAmountByProjectId(@Param("projectId") Long projectId);
    
    /**
     * Count payments for a specific invoice.
     * 
     * @param invoiceId The invoice ID
     * @return The count of payments
     */
    long countByInvoiceId(Long invoiceId);
    
    /**
     * Find most recent payments with pagination.
     * 
     * @param pageable Pagination information
     * @return A page of recent payments
     */
    @Query("SELECT p FROM Payment p ORDER BY p.paymentDate DESC")
    @EntityGraph(attributePaths = {"invoice"})
    Page<Payment> findMostRecent(Pageable pageable);
}