package com.freelancer.portal.specification;

import com.freelancer.portal.model.Project;
import com.freelancer.portal.model.TimeEntry;
import com.freelancer.portal.model.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Specifications for TimeEntry entity to enable advanced filtering
 */
public class TimeEntrySpecification {

    /**
     * Filter time entries by project
     */
    public static Specification<TimeEntry> hasProject(Project project) {
        return (root, query, cb) -> {
            if (project == null) {
                return null;
            }
            return cb.equal(root.get("project"), project);
        };
    }

    /**
     * Filter time entries by project ID
     */
    public static Specification<TimeEntry> hasProjectId(Long projectId) {
        return (root, query, cb) -> {
            if (projectId == null) {
                return null;
            }
            return cb.equal(root.get("project").get("id"), projectId);
        };
    }

    /**
     * Filter time entries by user
     */
    public static Specification<TimeEntry> hasUser(User user) {
        return (root, query, cb) -> {
            if (user == null) {
                return null;
            }
            return cb.equal(root.get("user"), user);
        };
    }

    /**
     * Filter time entries by user ID
     */
    public static Specification<TimeEntry> hasUserId(Long userId) {
        return (root, query, cb) -> {
            if (userId == null) {
                return null;
            }
            return cb.equal(root.get("user").get("id"), userId);
        };
    }

    /**
     * Filter time entries by date range
     */
    public static Specification<TimeEntry> dateRangeBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            if (startDate == null && endDate == null) {
                return null;
            }
            
            LocalDateTime startDateTime = startDate != null ? 
                startDate.atStartOfDay() : LocalDateTime.MIN;
                
            LocalDateTime endDateTime = endDate != null ? 
                endDate.atTime(LocalTime.MAX) : LocalDateTime.MAX;
                
            return cb.between(root.get("startTime"), startDateTime, endDateTime);
        };
    }

    /**
     * Filter billable time entries
     */
    public static Specification<TimeEntry> isBillable(Boolean billable) {
        return (root, query, cb) -> {
            if (billable == null) {
                return null;
            }
            return cb.equal(root.get("billable"), billable);
        };
    }

    /**
     * Filter billed time entries
     */
    public static Specification<TimeEntry> isBilled(Boolean billed) {
        return (root, query, cb) -> {
            if (billed == null) {
                return null;
            }
            return cb.equal(root.get("billed"), billed);
        };
    }

    /**
     * Filter by description containing text
     */
    public static Specification<TimeEntry> descriptionContains(String text) {
        return (root, query, cb) -> {
            if (text == null || text.isEmpty()) {
                return null;
            }
            return cb.like(cb.lower(root.get("description")), "%" + text.toLowerCase() + "%");
        };
    }

    /**
     * Filter by time entries associated with an invoice
     */
    public static Specification<TimeEntry> hasInvoiceId(Long invoiceId) {
        return (root, query, cb) -> {
            if (invoiceId == null) {
                return null;
            }
            return cb.equal(root.get("invoice").get("id"), invoiceId);
        };
    }

    /**
     * Filter by time entries not associated with any invoice
     */
    public static Specification<TimeEntry> notInvoiced() {
        return (root, query, cb) -> cb.isNull(root.get("invoice"));
    }
    
    /**
     * Filter billable but unbilled time entries
     */
    public static Specification<TimeEntry> billableButNotBilled() {
        return (root, query, cb) -> 
            cb.and(
                cb.equal(root.get("billable"), true),
                cb.equal(root.get("billed"), false)
            );
    }
}