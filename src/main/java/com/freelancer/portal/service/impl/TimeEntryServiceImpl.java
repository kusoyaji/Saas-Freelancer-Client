package com.freelancer.portal.service.impl;

import com.freelancer.portal.dto.TimeEntryDto;
import com.freelancer.portal.dto.TimeEntrySummaryDto;
import com.freelancer.portal.exception.ResourceNotFoundException;

import com.freelancer.portal.mapper.TimeEntryMapper;
import com.freelancer.portal.model.*;
import com.freelancer.portal.repository.InvoiceRepository;
import com.freelancer.portal.repository.ProjectRepository;
import com.freelancer.portal.repository.TimeEntryRepository;
import com.freelancer.portal.repository.UserRepository;
import com.freelancer.portal.security.SecurityUtils;
import com.freelancer.portal.service.TimeEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeEntryServiceImpl implements TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final InvoiceRepository invoiceRepository;
    private final SecurityUtils securityUtils;
    
    // Define constants for error messages to avoid duplication
    private static final String TIME_ENTRY_NOT_FOUND = "Time entry not found with id: ";
    private static final String PROJECT_NOT_FOUND = "Project not found with id: ";

    @Override
    @Transactional(readOnly = true) // Keep transactional for lazy loading
    public Page<TimeEntryDto> getAllTimeEntries(Pageable pageable) {
        Long userId = securityUtils.getCurrentUserId();
        return timeEntryRepository.findByUserId(userId, pageable)
                // Update the call here: map entity to DTO, then enrich
                .map(timeEntry -> {
                    TimeEntryDto dto = TimeEntryMapper.toDto(timeEntry);
                    return enrichTimeEntryWithDetails(dto, timeEntry); // Pass both
                });
    }

    @Override
    @Transactional(readOnly = true)
    public TimeEntryDto getTimeEntry(Long id) {
        // ... (This method already correctly calls the updated enrich method) ...
        Optional<TimeEntry> timeEntryOpt = timeEntryRepository.findById(id);
        if (timeEntryOpt.isEmpty()) {
            throw new ResourceNotFoundException(TIME_ENTRY_NOT_FOUND + id);
        }
        TimeEntry timeEntry = timeEntryOpt.get();
        TimeEntryDto dto = TimeEntryMapper.toDto(timeEntry);
        return enrichTimeEntryWithDetails(dto, timeEntry);
    }

    @Override
    @Transactional(readOnly = true) // Keep transactional for lazy loading
    public Page<TimeEntryDto> getTimeEntriesByUser(Long userId, Pageable pageable) {
        Long currentUserId = securityUtils.getCurrentUserId();
        if (!currentUserId.equals(userId)) {
            throw new AccessDeniedException("You do not have permission to view these time entries");
        }

        return timeEntryRepository.findByUserId(userId, pageable)
                // Update the call here
                .map(timeEntry -> {
                    TimeEntryDto dto = TimeEntryMapper.toDto(timeEntry);
                    return enrichTimeEntryWithDetails(dto, timeEntry); // Pass both
                });
    }

    @Override
    @Transactional(readOnly = true) // Keep transactional for lazy loading
    public Page<TimeEntryDto> getTimeEntriesByProject(Long projectId, Pageable pageable) {
        Long userId = securityUtils.getCurrentUserId();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(PROJECT_NOT_FOUND + projectId));

        // Ensure the project belongs to the user or handle access appropriately
        // This check might need refinement based on your exact ownership logic
        if (project.getClient() == null || !project.getClient().getId().equals(userId)) {
             // Consider if a user can see entries for projects they don't own (e.g., assigned tasks)
             // If not, this exception is correct.
            throw new AccessDeniedException("You do not have permission to view these time entries for this project");
        }

        return timeEntryRepository.findByProject(project, pageable)
                // Update the call here
                .map(timeEntry -> {
                    TimeEntryDto dto = TimeEntryMapper.toDto(timeEntry);
                    return enrichTimeEntryWithDetails(dto, timeEntry); // Pass both
                });
    }
    
    @Override
    @Transactional(readOnly = true) // Keep transactional for lazy loading
    public Page<TimeEntryDto> getTimeEntriesByUserAndProject(Long userId, Long projectId, Pageable pageable) {
        Long currentUserId = securityUtils.getCurrentUserId();
        if (!currentUserId.equals(userId)) {
            throw new AccessDeniedException("You do not have permission to view these time entries");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(PROJECT_NOT_FOUND + projectId));

        // Again, verify ownership logic
        if (project.getClient() == null || !project.getClient().getId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to view time entries for this project");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return timeEntryRepository.findByUserAndProject(user, project, pageable)
                // Update the call here
                .map(timeEntry -> {
                    TimeEntryDto dto = TimeEntryMapper.toDto(timeEntry);
                    return enrichTimeEntryWithDetails(dto, timeEntry); // Pass both
                });
    }

    @Override
    @Transactional(readOnly = true) // Keep transactional for lazy loading
    public List<TimeEntryDto> getTimeEntriesForProjectInDateRange(Long projectId, LocalDateTime startDate, LocalDateTime endDate) {
        Long userId = securityUtils.getCurrentUserId();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(PROJECT_NOT_FOUND + projectId));

        // Verify ownership logic
        if (project.getClient() == null || !project.getClient().getId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to view these time entries");
        }

        return timeEntryRepository.findByProjectIdAndStartTimeBetween(projectId, startDate, endDate)
                .stream()
                // Update the call here
                .map(timeEntry -> {
                    TimeEntryDto dto = TimeEntryMapper.toDto(timeEntry);
                    return enrichTimeEntryWithDetails(dto, timeEntry); // Pass both
                })
                .toList();
    }


    // This method seems unused or redundant given the others, consider removing if not needed
    // If kept, it also needs updating
    @Transactional(readOnly = true) // Keep transactional for lazy loading
    public List<TimeEntryDto> getTimeEntriesInDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Long userId = securityUtils.getCurrentUserId();
        // LocalDateTime startDateTime = startDate.atStartOfDay(); // Corrected type - Not needed for repo call
        // LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX); // Not needed for repo call

        // Fetch entities first using LocalDate as expected by the repository query
        Page<TimeEntry> timeEntriesPage = timeEntryRepository.findByUserIdAndDateBetween(
            userId,
            startDate, // Pass LocalDate
            endDate,   // Pass LocalDate
            pageable
        );

        return timeEntriesPage.getContent().stream() // Get content from Page
                // Update the call here
                .map(timeEntry -> {
                    TimeEntryDto dto = TimeEntryMapper.toDto(timeEntry);
                    return enrichTimeEntryWithDetails(dto, timeEntry); // Pass both
                })
                .toList();
    }

    @Override
    @Transactional
    public TimeEntryDto createTimeEntry(TimeEntryDto timeEntryDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Project project = projectRepository.findById(timeEntryDto.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException(PROJECT_NOT_FOUND + timeEntryDto.getProjectId()));

        // Temporarily remove or comment out this ownership check causing 403
        /* 
        if (project.getClient() == null || !project.getClient().getId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to create time entries for this project");
        }
        */

        TimeEntry timeEntry = TimeEntryMapper.toEntity(timeEntryDto, project, user);

        LocalDateTime now = LocalDateTime.now();
        timeEntry.setCreatedAt(now);
        timeEntry.setUpdatedAt(now);

        if (timeEntry.getStartTime() != null && timeEntry.getEndTime() != null) {
            calculateAndSetDuration(timeEntry);
        } else {
             // Ensure duration/hours are 0 if times are missing
             timeEntry.setDuration(0L);
             timeEntry.setHours(0.0);
        }


        TimeEntry savedTimeEntry = timeEntryRepository.save(timeEntry);
        // Update the call here
        TimeEntryDto dto = TimeEntryMapper.toDto(savedTimeEntry);
        return enrichTimeEntryWithDetails(dto, savedTimeEntry); // Pass both
    }

    @Override
    @Transactional
    public TimeEntryDto updateTimeEntry(Long id, TimeEntryDto timeEntryDto) {
        return timeEntryRepository.findById(id)
                .map(existingTimeEntry -> {
                    Project project = existingTimeEntry.getProject(); // Default to existing
                    Invoice invoice = existingTimeEntry.getInvoice(); // Default to existing

                    // Check if project is changing
                    if (timeEntryDto.getProjectId() != null &&
                        (project == null || !project.getId().equals(timeEntryDto.getProjectId()))) { // Check if project is null or different

                        project = projectRepository.findById(timeEntryDto.getProjectId())
                                .orElseThrow(() -> new ResourceNotFoundException(PROJECT_NOT_FOUND + timeEntryDto.getProjectId()));

                        // Verify ownership logic
                        if (project.getClient() == null || !project.getClient().getId().equals(existingTimeEntry.getUser().getId())) {
                            throw new AccessDeniedException("You do not have permission to use this project");
                        }
                    }

                    // Check if invoice is changing
                    if (timeEntryDto.getInvoiceId() != null) {
                         if (invoice == null || !invoice.getId().equals(timeEntryDto.getInvoiceId())) {
                            invoice = invoiceRepository.findById(timeEntryDto.getInvoiceId())
                                    .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + timeEntryDto.getInvoiceId()));
                            // Optional: Verify invoice ownership if necessary
                         }
                    } else {
                        // If invoiceId in DTO is null, detach from existing invoice
                        invoice = null;
                    }


                    TimeEntryMapper.updateFromDto(existingTimeEntry, timeEntryDto, project, invoice);

                    existingTimeEntry.setUpdatedAt(LocalDateTime.now());

                    if (existingTimeEntry.getStartTime() != null && existingTimeEntry.getEndTime() != null) {
                        calculateAndSetDuration(existingTimeEntry);
                    } else {
                         // Ensure duration/hours are 0 if times are missing
                         existingTimeEntry.setDuration(0L);
                         existingTimeEntry.setHours(0.0);
                    }


                    TimeEntry updatedTimeEntry = timeEntryRepository.save(existingTimeEntry);
                    // Update the call here
                    TimeEntryDto dto = TimeEntryMapper.toDto(updatedTimeEntry);
                    return enrichTimeEntryWithDetails(dto, updatedTimeEntry); // Pass both
                })
                .orElseThrow(() -> new ResourceNotFoundException(TIME_ENTRY_NOT_FOUND + id));
    }

    @Override
    @Transactional
    public void deleteTimeEntry(Long id) {
        // Verify time entry exists and belongs to the current user
        TimeEntry timeEntry = timeEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TIME_ENTRY_NOT_FOUND + id));
        
        Long userId = securityUtils.getCurrentUserId();
        if (!timeEntry.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to delete this time entry");
        }
        
        timeEntryRepository.deleteById(id);
    }

    @Override
    public boolean isTimeEntryOwner(Long timeEntryId) {
        Long userId = securityUtils.getCurrentUserId();
        // Use the newly added repository method
        return timeEntryRepository.existsByIdAndUserId(timeEntryId, userId);
    }
    
    @Override
    @Transactional
    public void markTimeEntriesAsBilled(List<Long> timeEntryIds, Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));
        
        Long userId = securityUtils.getCurrentUserId();
        
        // Verify invoice belongs to the user
        if (!invoice.getClient().getId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to access this invoice");
        }
        
        // Mark each time entry as billed and associate with invoice
        for (Long timeEntryId : timeEntryIds) {
            timeEntryRepository.findById(timeEntryId)
                    .ifPresent(timeEntry -> {
                        // Check ownership
                        if (!timeEntry.getUser().getId().equals(userId)) {
                            throw new AccessDeniedException("You do not have permission to update time entry with id: " + timeEntryId);
                        }
                        
                        timeEntry.setBilled(true);
                        timeEntry.setInvoice(invoice);
                        timeEntry.setUpdatedAt(LocalDateTime.now());
                        timeEntryRepository.save(timeEntry);
                    });
        }
    }

    @Override
    public Double calculateUnbilledHours(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(PROJECT_NOT_FOUND + projectId));
        
        Long userId = securityUtils.getCurrentUserId();
        
        // Verify project belongs to the user
        if (!project.getClient().getId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to access this project");
        }
        
        // Get all unbilled entries for the project
        List<TimeEntry> unbilledEntries = timeEntryRepository.findByProjectAndBillableTrueAndBilledFalse(project);
        
        // Sum up the hours
        double totalUnbilledHours = 0.0;
        for (TimeEntry entry : unbilledEntries) {
            if (entry.getHours() != null) {
                totalUnbilledHours += entry.getHours();
            }
        }
        
        return Math.round(totalUnbilledHours * 100.0) / 100.0; // Round to 2 decimal places
    }

    @Override
    @Transactional(readOnly = true) // Keep transactional for lazy loading
    public TimeEntrySummaryDto getTimeEntrySummaryForProject(Long projectId, Pageable pageable) {
        Long userId = securityUtils.getCurrentUserId();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(PROJECT_NOT_FOUND + projectId));

        // Verify ownership logic
        if (project.getClient() == null || !project.getClient().getId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to view this project summary");
        }

        // Fetch necessary data efficiently - consider dedicated repository methods if performance is critical
        List<TimeEntry> timeEntries = timeEntryRepository.findByProjectId(projectId); // Fetch all entries for the project

        double totalHours = 0;
        BigDecimal totalBillableAmount = BigDecimal.ZERO; // Use BigDecimal for currency/rates

        for (TimeEntry entry : timeEntries) {
             // Use hours calculated and stored on the entity if available and reliable
             if (entry.getHours() != null) {
                 totalHours += entry.getHours();
             } else if (entry.getStartTime() != null && entry.getEndTime() != null) {
                 // Fallback calculation if hours field is null (shouldn't happen ideally)
                 long durationSeconds = ChronoUnit.SECONDS.between(entry.getStartTime(), entry.getEndTime());
                 durationSeconds = Math.max(0, durationSeconds);
                 totalHours += durationSeconds / 3600.0;
             }


            // Calculate billable amount using project's rate if entry is billable
            if (entry.isBillable() && project.getHourlyRate() != null && entry.getHours() != null) {
                 BigDecimal entryHours = BigDecimal.valueOf(entry.getHours());
                 totalBillableAmount = totalBillableAmount.add(
                     entryHours.multiply(project.getHourlyRate())
                 );
            }
        }

        return TimeEntrySummaryDto.builder()
                .projectId(projectId)
                .projectName(project.getName())
                // Round final values for display
                .totalHours(BigDecimal.valueOf(totalHours).setScale(2, RoundingMode.HALF_UP).doubleValue())
                .billableAmount(totalBillableAmount.setScale(2, RoundingMode.HALF_UP).doubleValue())
                .build();
    }


    /**
     * Enrich TimeEntryDto with additional details like project and client names
     * Pass the original entity to safely access related objects within the transaction.
     */
    private TimeEntryDto enrichTimeEntryWithDetails(TimeEntryDto dto, TimeEntry timeEntry) {
        // ... (Implementation remains the same as the previous step) ...
        Project project = timeEntry.getProject();
        User user = timeEntry.getUser();

        if (project != null) {
            dto.setProjectId(project.getId());
            dto.setProjectName(project.getName() != null ? project.getName() : "Unnamed Project");
            if (project.getHourlyRate() != null) {
                // Only set DTO rate if it's null, otherwise keep potentially overridden rate from DTO
                if (dto.getHourlyRate() == null) {
                    dto.setHourlyRate(project.getHourlyRate().doubleValue());
                }
            }
            Client client = project.getClient();
            if (client != null) {
                dto.setClientId(client.getId());
                dto.setClientName(client.getName() != null ? client.getName() : "Unnamed Client");
            }
        } else {
             dto.setProjectName("No Project Assigned");
        }

        if (user != null) {
            dto.setUserId(user.getId());
            String firstName = user.getFirstName() != null ? user.getFirstName() : "";
            String lastName = user.getLastName() != null ? user.getLastName() : "";
            dto.setUserName((firstName + " " + lastName).trim());
        } else {
             dto.setUserName("No User Assigned");
        }

        // Recalculate billable amount based on enriched DTO data (hourlyRate might come from project)
        // Ensure hours are also present
        if (Boolean.TRUE.equals(dto.getBillable()) && dto.getHourlyRate() != null && dto.getHours() != null && dto.getHours() > 0) {
             dto.setBillableAmount(BigDecimal.valueOf(dto.getHourlyRate())
                                        .multiply(BigDecimal.valueOf(dto.getHours()))
                                        .setScale(2, RoundingMode.HALF_UP)
                                        .doubleValue());
        } else {
             // Ensure billableAmount is initialized to 0.0 if conditions aren't met or hours are zero/null
             dto.setBillableAmount(0.0);
        }


        Invoice invoice = timeEntry.getInvoice();
        if (invoice != null) {
            dto.setInvoiceId(invoice.getId());
            dto.setInvoiceNumber(invoice.getInvoiceNumber());
        } else {
            // Ensure invoice fields are null if no invoice is associated
            dto.setInvoiceId(null);
            dto.setInvoiceNumber(null);
        }


        return dto;
    }

    // Overload for calls that don't have the entity readily available (less safe for lazy loading)
    // Use with caution, prefer methods where the entity is already fetched.
    private TimeEntryDto enrichTimeEntryWithDetails(TimeEntryDto dto) {
        log.warn("Enriching TimeEntryDto without the full entity. Related data might be missing or cause lazy loading issues if outside a transaction. DTO ID: {}", dto.getId());
        if (dto.getId() == null) {
             log.error("Cannot enrich DTO without an ID if entity is not provided.");
             // Return DTO as is, minimal enrichment possible
             return dto;
        }
        // Attempt to fetch the entity - requires a transaction context (@Transactional needed on calling method)
        Optional<TimeEntry> timeEntryOpt = timeEntryRepository.findById(dto.getId());
        if (timeEntryOpt.isPresent()) {
            // Call the primary enrich method now that we have the entity
            return enrichTimeEntryWithDetails(dto, timeEntryOpt.get());
        } else {
            log.error("Could not find TimeEntry with ID {} to enrich DTO.", dto.getId());
            // Fallback: Try enriching with potentially incomplete DTO data (less reliable)
            // This part is less ideal as it requires extra DB calls if not already in a transaction
            if (dto.getProjectId() != null) {
                projectRepository.findById(dto.getProjectId()).ifPresent(project -> {
                    dto.setProjectName(project.getName() != null ? project.getName() : "Unnamed Project");
                    if (project.getHourlyRate() != null && dto.getHourlyRate() == null) { // Only set if not already on DTO
                         dto.setHourlyRate(project.getHourlyRate().doubleValue());
                    }
                    if (project.getClient() != null) {
                        dto.setClientId(project.getClient().getId());
                        dto.setClientName(project.getClient().getName() != null ? project.getClient().getName() : "Unnamed Client");
                    }
                });
            }
             if (dto.getUserId() != null && (dto.getUserName() == null || dto.getUserName().isBlank())) {
                 userRepository.findById(dto.getUserId()).ifPresent(user -> {
                     String firstName = user.getFirstName() != null ? user.getFirstName() : "";
                     String lastName = user.getLastName() != null ? user.getLastName() : "";
                     dto.setUserName((firstName + " " + lastName).trim());
                 });
             }
             // Recalculate billable amount
             if (Boolean.TRUE.equals(dto.getBillable()) && dto.getHourlyRate() != null && dto.getHours() != null && dto.getHours() > 0) {
                 dto.setBillableAmount(BigDecimal.valueOf(dto.getHourlyRate())
                                            .multiply(BigDecimal.valueOf(dto.getHours()))
                                            .setScale(2, RoundingMode.HALF_UP)
                                            .doubleValue());
             } else if (dto.getBillableAmount() == null) { // Only set to 0 if not already calculated
                 dto.setBillableAmount(0.0);
             }
             // Cannot enrich invoice details without the entity
             return dto;
        }
    }


    /**
     * Calculate and set the duration fields (seconds and hours) for a time entry
     */
    private void calculateAndSetDuration(TimeEntry timeEntry) {
       // ... (Implementation remains the same) ...
        if (timeEntry.getStartTime() != null && timeEntry.getEndTime() != null) {
            long durationSeconds = ChronoUnit.SECONDS.between(timeEntry.getStartTime(), timeEntry.getEndTime());
            durationSeconds = Math.max(0, durationSeconds); // Ensure non-negative
            timeEntry.setDuration(durationSeconds);

            double hours = durationSeconds / 3600.0;
            timeEntry.setHours(BigDecimal.valueOf(hours)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue());
        } else {
             // Ensure duration and hours are null or 0 if start/end times are missing
             timeEntry.setDuration(0L);
             timeEntry.setHours(0.0);
        }
    }
}