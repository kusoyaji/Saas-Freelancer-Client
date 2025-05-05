package com.freelancer.portal.controller;

import com.freelancer.portal.dto.TimeEntryDto;
import com.freelancer.portal.dto.TimeEntrySummaryDto;
import com.freelancer.portal.security.SecurityUtils;
import com.freelancer.portal.service.TimeEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * REST Controller for time entry operations.
 */
@RestController
@RequestMapping("/time-entries")
@RequiredArgsConstructor
public class TimeEntryController {

    private final TimeEntryService timeEntryService;
    private final SecurityUtils securityUtils;
    /**
     * Get all time entries for the current user with pagination.
     */
    @GetMapping
    public ResponseEntity<Page<TimeEntryDto>> getAllTimeEntries(Pageable pageable) {
        return ResponseEntity.ok(timeEntryService.getAllTimeEntries(pageable));
    }
    
    /**
     * Get a time entry by ID.
     * Only accessible if the time entry belongs to the current user.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<TimeEntryDto> getTimeEntryById(@PathVariable Long id) {
        return ResponseEntity.ok(timeEntryService.getTimeEntry(id));
    }
    
    /**
     * Get time entries for a specific project with pagination.
     * Only accessible if the project belongs to the current user.
     */
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<Page<TimeEntryDto>> getTimeEntriesByProject(
            @PathVariable Long projectId, Pageable pageable) {
        try {
            return ResponseEntity.ok(timeEntryService.getTimeEntriesByProject(projectId, pageable));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get time entries for a specific client with pagination.
     * Only accessible if the client belongs to the current user.
     */
    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<Page<TimeEntryDto>> getTimeEntriesByClient(
            @PathVariable Long clientId, Pageable pageable) {
        // Get projects for client first, then get time entries for those projects
        try {
            Long userId = securityUtils.getCurrentUserId();
            return ResponseEntity.ok(timeEntryService.getTimeEntriesByUser(userId, pageable));
            // In a real implementation, we would filter by client's projects
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get time entries for a date range for a specific project.
     */
    @GetMapping("/project/{projectId}/date-range")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<List<TimeEntryDto>> getTimeEntriesByProjectAndDateRange(
            @PathVariable Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate) {
        return ResponseEntity.ok(timeEntryService.getTimeEntriesForProjectInDateRange(projectId, startDate, endDate));
    }

    /**
     * Get time entries for a date range.
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<TimeEntryDto>> getTimeEntriesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        // Convert LocalDate to LocalDateTime for service method
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        // Get current user's projects and aggregate time entries
        Long userId = securityUtils.getCurrentUserId();
        List<TimeEntryDto> entries = timeEntryService.getTimeEntriesByUser(userId, Pageable.unpaged()).getContent();
        
        // Filter by date range
        return ResponseEntity.ok(entries.stream()
                .filter(entry -> {
                    LocalDateTime entryTime = entry.getStartTime();
                    return entryTime != null && 
                           !entryTime.isBefore(startDateTime) && 
                           !entryTime.isAfter(endDateTime);
                })
                .collect(Collectors.toList()));
    }
//
    /**
     * Get summary of time entries grouped by project for a date range.
     */
    @GetMapping("/summary/project")
    public ResponseEntity<Map<String, Double>> getTimeEntrySummaryByProject(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        // Get all user's time entries and group by project
        Long userId = securityUtils.getCurrentUserId();
        List<TimeEntryDto> entries = timeEntryService.getTimeEntriesByUser(userId, Pageable.unpaged()).getContent();
        
        // Filter by date range and group by project
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        Map<String, Double> projectSummary = entries.stream()
                .filter(entry -> {
                    LocalDateTime entryTime = entry.getStartTime();
                    return entryTime != null && 
                           !entryTime.isBefore(startDateTime) && 
                           !entryTime.isAfter(endDateTime);
                })
                .collect(Collectors.groupingBy(
                    TimeEntryDto::getProjectName,
                    Collectors.summingDouble(TimeEntryDto::getHours)
                ));
        
        return ResponseEntity.ok(projectSummary);
    }

    /**
     * Get summary of time entries grouped by client for a date range.
     */
    @GetMapping("/summary/client")
    public ResponseEntity<Map<String, Double>> getTimeEntrySummaryByClient(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        // In a real implementation, we would group by client
        // For now, we'll return project summary as a placeholder
        return getTimeEntrySummaryByProject(startDate, endDate);
    }

    /**
     * Get detailed time entry summary for a project.
     */
    @GetMapping("/summary/project/{projectId}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<TimeEntrySummaryDto> getDetailedTimeEntrySummary(
            @PathVariable Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(timeEntryService.getTimeEntrySummaryForProject(projectId, Pageable.unpaged()));
    }
//
    /**
     * Get total hours logged for a specific period.
     */
    @GetMapping("/total-hours")
    public ResponseEntity<Double> getTotalHours(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        // Get all user's time entries and sum hours
        Long userId = securityUtils.getCurrentUserId();
        List<TimeEntryDto> entries = timeEntryService.getTimeEntriesByUser(userId, Pageable.unpaged()).getContent();
        
        // Filter by date range and sum hours
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        Double totalHours = entries.stream()
                .filter(entry -> {
                    LocalDateTime entryTime = entry.getStartTime();
                    return entryTime != null && 
                           !entryTime.isBefore(startDateTime) && 
                           !entryTime.isAfter(endDateTime);
                })
                .mapToDouble(TimeEntryDto::getHours)
                .sum();
        
        return ResponseEntity.ok(totalHours);
    }

    /**
     * Create a new time entry.
     */
    @PostMapping
    public ResponseEntity<TimeEntryDto> createTimeEntry(@Valid @RequestBody TimeEntryDto timeEntryDto) {
        Long userId = securityUtils.getCurrentUserId();
        return new ResponseEntity<>(timeEntryService.createTimeEntry(timeEntryDto, userId), HttpStatus.CREATED);
    }

    @PostMapping("/new")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<TimeEntryDto> createTimeEntryagain(@Valid @RequestBody TimeEntryDto timeEntryDto) {
        Long userId = securityUtils.getCurrentUserId();
        return new ResponseEntity<>(timeEntryService.createTimeEntry(timeEntryDto, userId), HttpStatus.CREATED);
    }

    /**
     * Update an existing time entry.
     * Only accessible if the time entry belongs to the current user.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<TimeEntryDto> updateTimeEntry(
            @PathVariable Long id, @Valid @RequestBody TimeEntryDto timeEntryDto) {
        return ResponseEntity.ok(timeEntryService.updateTimeEntry(id, timeEntryDto));
    }
//
    /**
     * Start a time tracking session.
     */
    @PostMapping("/start")
    public ResponseEntity<TimeEntryDto> startTimeTracking(@Valid @RequestBody TimeEntryDto timeEntryDto) {
        // Set start time to now and leave end time null
        timeEntryDto.setStartTime(LocalDateTime.now());
        timeEntryDto.setEndTime(null);
        
        Long userId = securityUtils.getCurrentUserId();
        return new ResponseEntity<>(timeEntryService.createTimeEntry(timeEntryDto, userId), HttpStatus.CREATED);
    }

    /**
     * Stop an ongoing time tracking session.
     * Only accessible if the time entry belongs to the current user.
     */
    @PutMapping("/{id}/stop")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<TimeEntryDto> stopTimeTracking(@PathVariable Long id) {
        // Get the time entry
        TimeEntryDto timeEntryDto = timeEntryService.getTimeEntry(id);
        
        // Set end time to now
        LocalDateTime endTime = LocalDateTime.now();
        timeEntryDto.setEndTime(endTime);
        
        // Calculate duration in seconds and hours
        if (timeEntryDto.getStartTime() != null) {
            LocalDateTime startTime = timeEntryDto.getStartTime();
            long durationSeconds = java.time.Duration.between(startTime, endTime).getSeconds();
            timeEntryDto.setDurationSeconds(durationSeconds);
            
            // Calculate hours (rounded to 2 decimal places)
            double hours = durationSeconds / 3600.0;
            timeEntryDto.setHours(Math.round(hours * 100.0) / 100.0);
            
            // Also set the duration in minutes for consistency
            timeEntryDto.setDuration((int) (durationSeconds / 60));
        }
        
        return ResponseEntity.ok(timeEntryService.updateTimeEntry(id, timeEntryDto));
    }

    /**
     * Delete a time entry.
     * Only accessible if the time entry belongs to the current user.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<Void> deleteTimeEntry(@PathVariable Long id) {
        timeEntryService.deleteTimeEntry(id);
        return ResponseEntity.noContent().build();
    }
//
    /**
     * Get currently active time entries for the current user.
     */
    @GetMapping("/active")
    public ResponseEntity<List<TimeEntryDto>> getActiveTimeEntries() {
        // Get all user's time entries
        Long userId = securityUtils.getCurrentUserId();
        List<TimeEntryDto> entries = timeEntryService.getTimeEntriesByUser(userId, Pageable.unpaged()).getContent();
        
        // Filter for active entries (start time exists but no end time)
        List<TimeEntryDto> activeEntries = entries.stream()
                .filter(entry -> entry.getStartTime() != null && entry.getEndTime() == null)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(activeEntries);
    }

    /**
     * Get time entries that are billable but not yet invoiced.
     */
    @GetMapping("/billable-not-invoiced")
    public ResponseEntity<List<TimeEntryDto>> getBillableNotInvoicedTimeEntries() {
        // Get all user's time entries
        Long userId = securityUtils.getCurrentUserId();
        List<TimeEntryDto> entries = timeEntryService.getTimeEntriesByUser(userId, Pageable.unpaged()).getContent();
        
        // Filter for billable but not invoiced entries
        List<TimeEntryDto> billableNotInvoiced = entries.stream()
                .filter(entry -> Boolean.TRUE.equals(entry.getBillable()) && 
                                 (entry.getInvoiceId() == null || !Boolean.TRUE.equals(entry.getBilled())))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(billableNotInvoiced);
    }

    /**
     * Mark multiple time entries as invoiced.
     */
    @PatchMapping("/mark-invoiced")
    public ResponseEntity<List<TimeEntryDto>> markTimeEntriesAsInvoiced(@RequestBody List<Long> timeEntryIds) {
        List<TimeEntryDto> updatedEntries = new ArrayList<>();
        
        for (Long id : timeEntryIds) {
            // Get the time entry
            TimeEntryDto timeEntryDto = timeEntryService.getTimeEntry(id);
            
            // Mark as billed
            timeEntryDto.setBilled(true);
            
            // Update the time entry
            updatedEntries.add(timeEntryService.updateTimeEntry(id, timeEntryDto));
        }
        
        return ResponseEntity.ok(updatedEntries);
    }
}