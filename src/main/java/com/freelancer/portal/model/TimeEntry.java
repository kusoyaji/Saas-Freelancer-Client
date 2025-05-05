package com.freelancer.portal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Entity representing a time entry record for project time tracking.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "time_entries")
public class TimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_seconds")
    private Long duration;

    @Column(name = "hours")
    private Double hours;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_billable", nullable = false)
    private boolean billable;

    @Column(name = "is_billed", nullable = false)
    private boolean billed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (startTime != null && endTime != null) {
            calculateDuration();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (startTime != null && endTime != null) {
            calculateDuration();
        }
    }

    /**
     * Calculate duration in seconds and hours from start and end time.
     */
    private void calculateDuration() {
        if (startTime != null && endTime != null) {
            duration = Duration.between(startTime, endTime).getSeconds();
            hours = duration / 3600.0; // Convert seconds to hours
        }
    }
}