package com.freelancer.portal.service;

import com.freelancer.portal.dto.TimeEntryDto;
import com.freelancer.portal.dto.TimeEntrySummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing time entries.
 */
public interface TimeEntryService {

    /**
     * Create a new time entry.
     *
     * @param timeEntryDto the time entry data
     * @param userId the user ID creating the entry
     * @return the created time entry
     */
    TimeEntryDto createTimeEntry(TimeEntryDto timeEntryDto, Long userId);
    
    /**
     * Get time entry by ID.
     *
     * @param id the time entry ID
     * @return the time entry
     */
    TimeEntryDto getTimeEntry(Long id);
    
    /**
     * Update a time entry.
     *
     * @param id the time entry ID
     * @param timeEntryDto the updated time entry data
     * @return the updated time entry
     */
    TimeEntryDto updateTimeEntry(Long id, TimeEntryDto timeEntryDto);
    
    /**
     * Delete a time entry.
     *
     * @param id the time entry ID
     */
    void deleteTimeEntry(Long id);
    
    /**
     * Get all time entries for a project.
     *
     * @param projectId the project ID
     * @param pageable pagination information
     * @return page of time entries
     */
    Page<TimeEntryDto> getTimeEntriesByProject(Long projectId, Pageable pageable) throws Exception;
    
    /**
     * Get all time entries for a user.
     *
     * @param userId the user ID
     * @param pageable pagination information
     * @return page of time entries
     */
    Page<TimeEntryDto> getTimeEntriesByUser(Long userId, Pageable pageable);
    
    /**
     * Get time entries for a user on a specific project.
     *
     * @param userId the user ID
     * @param projectId the project ID
     * @param pageable pagination information
     * @return page of time entries
     */
    Page<TimeEntryDto> getTimeEntriesByUserAndProject(Long userId, Long projectId, Pageable pageable);
    
    /**
     * Get time entries for a project within a date range.
     *
     * @param projectId the project ID
     * @param startTime the start datetime
     * @param endTime the end datetime
     * @return list of time entries
     */
    List<TimeEntryDto> getTimeEntriesForProjectInDateRange(Long projectId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get summary of time entries for a project.
     *
     * @param projectId the project ID
     * @return time entry summary
     */
    TimeEntrySummaryDto getTimeEntrySummaryForProject(Long projectId, Pageable pageable);
    /**
     * Mark time entries as billed for an invoice.
     *
     * @param timeEntryIds list of time entry IDs
     * @param invoiceId the invoice ID
     */
    void markTimeEntriesAsBilled(List<Long> timeEntryIds, Long invoiceId);
    
    /**
     * Calculate billable but unbilled hours for a project.
     *
     * @param projectId the project ID
     * @return unbilled hours
     */
    Double calculateUnbilledHours(Long projectId);

    /**
     * Check if the current user is the owner of the time entry.
     *
     * @param timeEntryId the time entry ID
     * @return true if the current user owns the time entry, false otherwise
     */
    boolean isTimeEntryOwner(Long timeEntryId);

    Page<TimeEntryDto> getAllTimeEntries(Pageable pageable);
}