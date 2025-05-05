package com.freelancer.portal.repository;

import com.freelancer.portal.model.Client;
import com.freelancer.portal.model.Invoice;
import com.freelancer.portal.model.Project;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {
    
    /**
     * Find invoices by project with eager loading of related entities.
     */
    @EntityGraph(attributePaths = {"items", "client"})
    Page<Invoice> findByProject(Project project, Pageable pageable);
    
    /**
     * Find all invoices for a project with eager loading of items.
     */
    @EntityGraph(attributePaths = {"items"})
    List<Invoice> findByProject(Project project);
    
    /**
     * Find an invoice by ID and project with eager loading of items, payments, and client.
     */
    @EntityGraph(attributePaths = {"items", "payments", "client"})
    Optional<Invoice> findByIdAndProject(Long id, Project project);
    
    /**
     * Find invoices for multiple projects with pagination and eager loading.
     */
    @EntityGraph(attributePaths = {"items", "client"})
    Page<Invoice> findByProjectIn(List<Project> projects, Pageable pageable);
    
    /**
     * Find an invoice by invoice number with eager loading of related entities.
     */
    @EntityGraph(attributePaths = {"items", "payments", "client", "project"})
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    /**
     * Find invoices by status with eager loading of items and client.
     */
    @EntityGraph(attributePaths = {"items", "client"})
    List<Invoice> findByStatus(Invoice.Status status);
    
    /**
     * Find invoices with specific statuses and due date before the given date.
     * Used for identifying overdue invoices.
     * Optimized with specific fetch joins to avoid N+1 queries.
     */
    @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.client WHERE i.status IN :statuses AND i.dueDate < :dueDate")
    List<Invoice> findByStatusInAndDueDateBefore(
            @Param("statuses") List<Invoice.Status> statuses, 
            @Param("dueDate") LocalDateTime dueDate);

    /**
     * Find all invoices for a freelancer with pagination and eager loading.
     */
    @EntityGraph(attributePaths = {"client", "project"})
    Page<Invoice> findAllByFreelancer(User freelancer, Pageable pageable);

    /**
     * Find all invoices for a client with pagination and eager loading.
     */
    @EntityGraph(attributePaths = {"project"})
    Page<Invoice> findAllByClient(Client client, Pageable pageable);

    /**
     * Find invoices by freelancer and status with eager loading.
     */
    @EntityGraph(attributePaths = {"client", "project"})
    Page<Invoice> findAllByFreelancerAndStatus(User freelancer, Invoice.Status status, Pageable pageable);

    /**
     * Find invoices by freelancer and date range with eager loading.
     */
    @EntityGraph(attributePaths = {"client", "project"})
    List<Invoice> findAllByFreelancerAndIssueDateBetween(User freelancer, LocalDate startDate, LocalDate endDate);
    
    /**
     * Find all invoices belonging to specific projects with eager loading.
     */
    @EntityGraph(attributePaths = {"client", "items"})
    List<Invoice> findByProjectIn(List<Project> projects);

    /**
     * Find invoices by project ID with eager loading of items.
     */
    @EntityGraph(attributePaths = {"items"})
    List<Invoice> findByProjectId(Long projectId);
    
    /**
     * Count invoices by project ID.
     * Optimized to only perform a count operation.
     */
    long countByProjectId(Long projectId);
    
    /**
     * Find all unpaid invoices with eager loading.
     */
    @Query("SELECT i FROM Invoice i WHERE i.status NOT IN ('PAID', 'CANCELLED') AND i.dueDate < CURRENT_DATE")
    @EntityGraph(attributePaths = {"client"})
    List<Invoice> findAllOverdueInvoices();
    
    /**
     * Calculate total amount for invoices by project.
     * Optimized to perform calculation at database level.
     */
    @Query("SELECT SUM(i.amount) FROM Invoice i WHERE i.project.id = :projectId")
    BigDecimal calculateTotalAmountByProjectId(@Param("projectId") Long projectId);
}