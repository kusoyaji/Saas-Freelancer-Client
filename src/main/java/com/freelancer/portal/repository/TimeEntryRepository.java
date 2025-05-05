package com.freelancer.portal.repository;

import com.freelancer.portal.model.Project;
import com.freelancer.portal.model.TimeEntry;
import com.freelancer.portal.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for managing time entries with optimized queries
 */
@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long>, JpaSpecificationExecutor<TimeEntry> {

    /**
     * Find all time entries for a specific project with eager loading of user
     */
    @EntityGraph(attributePaths = {"user"})
    Page<TimeEntry> findByProjectId(Long projectId, Pageable pageable);
    
    /**
     * Find all time entries for a specific user with eager loading of project
     */
    @EntityGraph(attributePaths = {"project"})
    Page<TimeEntry> findByUserId(Long userId, Pageable pageable);
    
    /**
     * Find time entries for a specific user and project with eager loading
     */
    @EntityGraph(attributePaths = {"project", "user"})
    Page<TimeEntry> findByUserIdAndProjectId(Long userId, Long projectId, Pageable pageable);
    
    /**
     * Find time entries for a date range with optimized query
     */
    @Query("SELECT t FROM TimeEntry t LEFT JOIN FETCH t.project WHERE t.user.id = :userId AND DATE(t.startTime) >= :startDate AND DATE(t.startTime) <= :endDate")
    Page<TimeEntry> findByUserIdAndDateBetween(
        @Param("userId") Long userId, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate, 
        Pageable pageable
    );
    
    /**
     * Find unbilled time entries for a specific project with eager loading
     */
    @EntityGraph(attributePaths = {"user"})
    List<TimeEntry> findByProjectIdAndBilledFalse(Long projectId);
    
    /**
     * Calculate total hours logged for a project
     * Optimized to perform calculation at database level
     */
    @Query("SELECT COALESCE(SUM(t.hours), 0) FROM TimeEntry t WHERE t.project.id = :projectId")
    Double calculateTotalHoursForProject(@Param("projectId") Long projectId);
    
    /**
     * Calculate billable hours for a project
     * Optimized to perform calculation at database level
     */
    @Query("SELECT COALESCE(SUM(t.hours), 0) FROM TimeEntry t WHERE t.project.id = :projectId AND t.billable = true")
    Double calculateBillableHoursForProject(@Param("projectId") Long projectId);
    
    /**
     * Calculate billed hours for a project
     * Optimized to perform calculation at database level
     */
    @Query("SELECT COALESCE(SUM(t.hours), 0) FROM TimeEntry t WHERE t.project.id = :projectId AND t.billed = true")
    Double calculateBilledHoursForProject(@Param("projectId") Long projectId);
    
    /**
     * Calculate unbilled hours for a project
     * Optimized to perform calculation at database level
     */
    @Query("SELECT COALESCE(SUM(t.hours), 0) FROM TimeEntry t WHERE t.project.id = :projectId AND t.billable = true AND t.billed = false")
    Double calculateUnbilledHoursForProject(@Param("projectId") Long projectId);
    
    /**
     * Calculate monthly hours for a project with optimized query
     */
    @Query("SELECT COALESCE(SUM(t.hours), 0) FROM TimeEntry t WHERE t.project.id = :projectId " +
           "AND FUNCTION('YEAR', t.createdAt) = :year AND FUNCTION('MONTH', t.createdAt) = :month")
    Double calculateMonthlyHoursForProject(
        @Param("projectId") Long projectId, 
        @Param("year") int year, 
        @Param("month") int month
    );
    
    /**
     * Find all time entries by project and invoice with efficient fetching
     */
    @Query("SELECT t FROM TimeEntry t WHERE t.project.id = :projectId AND t.invoice.id = :invoiceId")
    @EntityGraph(attributePaths = {"user"})
    List<TimeEntry> findByProjectIdAndInvoiceId(
        @Param("projectId") Long projectId,
        @Param("invoiceId") Long invoiceId
    );

    /**
     * Check if a time entry exists with the given ID and belongs to the specified user.
     */
    boolean existsByIdAndUserId(Long id, Long userId);

    /**
     * Find time entries by project with eager loading of user
     */
    @EntityGraph(attributePaths = {"user"})
    Page<TimeEntry> findByProject(Project project, Pageable pageable);

    /**
     * Find time entries by user with eager loading of project
     */
    @EntityGraph(attributePaths = {"project"})
    Page<TimeEntry> findByUser(User user, Pageable pageable);

    /**
     * Find time entries by user and project with eager loading
     */
    @EntityGraph(attributePaths = {"invoice"})
    Page<TimeEntry> findByUserAndProject(User user, Project project, Pageable pageable);

    /**
     * Find time entries by project ID and date range with eager loading
     */
    @EntityGraph(attributePaths = {"user"})
    List<TimeEntry> findByProjectIdAndStartTimeBetween(Long projectId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find billable but unbilled time entries for a project
     */
    @EntityGraph(attributePaths = {"user"})
    List<TimeEntry> findByProjectAndBillableTrueAndBilledFalse(Project project);

    /**
     * Find all time entries for a project with eager loading
     */
    @EntityGraph(attributePaths = {"user"})
    List<TimeEntry> findByProjectId(Long projectId);
    
    /**
     * Find recent time entries for a user
     * Limited to improve performance
     */
    @Query(value = "SELECT t FROM TimeEntry t WHERE t.user.id = :userId ORDER BY t.startTime DESC")
    @EntityGraph(attributePaths = {"project"})
    List<TimeEntry> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);
}