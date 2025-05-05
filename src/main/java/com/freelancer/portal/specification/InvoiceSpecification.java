package com.freelancer.portal.specification;

import com.freelancer.portal.model.Invoice;
import com.freelancer.portal.model.Client;
import com.freelancer.portal.model.Project;
import com.freelancer.portal.model.User;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Specifications for Invoice entity to enable advanced filtering
 */
public class InvoiceSpecification {

    /**
     * Filter invoices by freelancer
     */
    public static Specification<Invoice> hasFreelancer(User freelancer) {
        return (root, query, cb) -> {
            if (freelancer == null) {
                return null;
            }
            return cb.equal(root.get("freelancer"), freelancer);
        };
    }

    /**
     * Filter invoices by client
     */
    public static Specification<Invoice> hasClient(Client client) {
        return (root, query, cb) -> {
            if (client == null) {
                return null;
            }
            return cb.equal(root.get("client"), client);
        };
    }

    /**
     * Filter invoices by client ID
     */
    public static Specification<Invoice> hasClientId(Long clientId) {
        return (root, query, cb) -> {
            if (clientId == null) {
                return null;
            }
            return cb.equal(root.get("client").get("id"), clientId);
        };
    }

    /**
     * Filter invoices by project
     */
    public static Specification<Invoice> hasProject(Project project) {
        return (root, query, cb) -> {
            if (project == null) {
                return null;
            }
            return cb.equal(root.get("project"), project);
        };
    }

    /**
     * Filter invoices by project ID
     */
    public static Specification<Invoice> hasProjectId(Long projectId) {
        return (root, query, cb) -> {
            if (projectId == null) {
                return null;
            }
            return cb.equal(root.get("project").get("id"), projectId);
        };
    }

    /**
     * Filter invoices by status
     */
    public static Specification<Invoice> hasStatus(Invoice.Status status) {
        return (root, query, cb) -> {
            if (status == null) {
                return null;
            }
            return cb.equal(root.get("status"), status);
        };
    }

    /**
     * Filter invoices by date range
     */
    public static Specification<Invoice> issueDateBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            if (startDate == null && endDate == null) {
                return null;
            }
            if (startDate == null) {
                return cb.lessThanOrEqualTo(root.get("issueDate"), endDate);
            }
            if (endDate == null) {
                return cb.greaterThanOrEqualTo(root.get("issueDate"), startDate);
            }
            return cb.between(root.get("issueDate"), startDate, endDate);
        };
    }

    /**
     * Filter by invoice number
     */
    public static Specification<Invoice> hasInvoiceNumber(String invoiceNumber) {
        return (root, query, cb) -> {
            if (invoiceNumber == null || invoiceNumber.isEmpty()) {
                return null;
            }
            return cb.like(cb.lower(root.get("invoiceNumber")), "%" + invoiceNumber.toLowerCase() + "%");
        };
    }

    /**
     * Filter by invoice amount greater than or equal
     */
    public static Specification<Invoice> amountGreaterThanOrEqual(BigDecimal amount) {
        return (root, query, cb) -> {
            if (amount == null) {
                return null;
            }
            return cb.greaterThanOrEqualTo(root.get("amount"), amount);
        };
    }

    /**
     * Filter by invoice amount less than or equal
     */
    public static Specification<Invoice> amountLessThanOrEqual(BigDecimal amount) {
        return (root, query, cb) -> {
            if (amount == null) {
                return null;
            }
            return cb.lessThanOrEqualTo(root.get("amount"), amount);
        };
    }

    /**
     * Filter overdue invoices
     */
    public static Specification<Invoice> isOverdue() {
        return (root, query, cb) -> {
            LocalDate today = LocalDate.now();
            return cb.and(
                cb.lessThan(root.get("dueDate"), today),
                cb.notEqual(root.get("status"), Invoice.Status.PAID),
                cb.notEqual(root.get("status"), Invoice.Status.CANCELLED)
            );
        };
    }
}