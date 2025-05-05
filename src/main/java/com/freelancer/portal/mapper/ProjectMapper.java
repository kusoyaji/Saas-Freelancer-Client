package com.freelancer.portal.mapper;

import com.freelancer.portal.dto.ProjectDetailDto;
import com.freelancer.portal.dto.ProjectDto;
import com.freelancer.portal.model.Client;
import com.freelancer.portal.model.Conversation;
import com.freelancer.portal.model.Invoice;
import com.freelancer.portal.model.Message;
import com.freelancer.portal.model.Project;
import com.freelancer.portal.model.User;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for mapping Project entities to DTOs and vice versa.
 */
public class ProjectMapper {

    /**
     * Map Project entity to ProjectDto with basic information.
     *
     * @param project the project entity
     * @return the project DTO with basic information
     */
    public static ProjectDto toDto(Project project) {
        return toDto(project, null, null, null, null, null, null, null, null, null);
    }

    /**
     * Map Project entity to ProjectDto with all related information.
     *
     * @param project the project entity
     * @param filesCount count of files associated with this project
     * @param invoicesCount count of invoices associated with this project
     * @param messagesCount count of messages associated with this project
     * @param conversationsCount count of conversations associated with this project
     * @param unreadMessagesCount count of unread messages associated with this project
     * @param invoicedAmount total amount invoiced for this project
     * @param paidAmount total amount paid for this project
     * @param pendingAmount total amount pending for this project
     * @return the project DTO with comprehensive information
     */
    public static ProjectDto toDto(
            Project project, 
            Integer filesCount,
            Integer invoicesCount, 
            Integer messagesCount,
            Integer conversationsCount,
            Integer unreadMessagesCount,
            BigDecimal invoicedAmount,
            BigDecimal paidAmount,
            BigDecimal pendingAmount,
            Long overduedInvoicesCount
    ) {
        if (project == null) {
            return null;
        }

        String clientName = null;
        String clientEmail = null;
        String clientPhone = null;
        Long clientId = null;

        if (project.getClient() != null) {
            clientName = project.getClient().getName();
            clientId = project.getClient().getId();
            clientEmail = project.getClient().getEmail();
            clientPhone = project.getClient().getPhone();
        }

        String freelancerName = null;
        String freelancerEmail = null;
        Long freelancerId = null;

        if (project.getFreelancer() != null) {
            freelancerId = project.getFreelancer().getId();
            freelancerName = project.getFreelancer().getFirstName() + " " + project.getFreelancer().getLastName();
            freelancerEmail = project.getFreelancer().getEmail();
        }

        // Use zeros instead of nulls for counts and amounts
        return ProjectDto.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .completedAt(project.getCompletedAt())
                .budget(project.getBudget())
                .hourlyRate(project.getHourlyRate())
                .clientId(clientId)
                .clientName(clientName)
                .clientEmail(clientEmail)
                .clientPhone(clientPhone)
                .freelancerId(freelancerId)
                .freelancerName(freelancerName)
                .freelancerEmail(freelancerEmail)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .filesCount(filesCount != null ? filesCount : 0)
                .invoicesCount(invoicesCount != null ? invoicesCount : 0)
                .messagesCount(messagesCount != null ? messagesCount : 0)
                .conversationsCount(conversationsCount != null ? conversationsCount : 0)
                .unreadMessagesCount(unreadMessagesCount != null ? unreadMessagesCount : 0)
                .invoicedAmount(invoicedAmount != null ? invoicedAmount : BigDecimal.ZERO)
                .paidAmount(paidAmount != null ? paidAmount : BigDecimal.ZERO)
                .pendingAmount(pendingAmount != null ? pendingAmount : BigDecimal.ZERO)
                .build();
    }

    /**
     * Map Project entity to ProjectDetailDto with detailed information including relationships.
     *
     * @param project the project entity
     * @param includeFiles whether to include file details
     * @param includeInvoices whether to include invoice details
     * @param includeMessages whether to include message details
     * @param unreadMessagesCount count of unread messages
     * @param invoicedAmount total amount invoiced
     * @param paidAmount total amount paid
     * @param pendingAmount total amount pending
     * @return the detailed project DTO
     */
    public static ProjectDetailDto toDetailDto(
            Project project,
            boolean includeFiles,
            boolean includeInvoices,
            boolean includeMessages,
            Long unreadMessagesCount,
            BigDecimal invoicedAmount,
            BigDecimal paidAmount,
            BigDecimal pendingAmount
    ) {
        if (project == null) {
            return null;
        }

        ProjectDetailDto.ProjectDetailDtoBuilder builder = ProjectDetailDto.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .completedAt(project.getCompletedAt())
                .budget(project.getBudget())
                .hourlyRate(project.getHourlyRate())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .invoicedAmount(invoicedAmount != null ? invoicedAmount : BigDecimal.ZERO)
                .paidAmount(paidAmount != null ? paidAmount : BigDecimal.ZERO)
                .pendingAmount(pendingAmount != null ? pendingAmount : BigDecimal.ZERO)
                .unreadMessagesCount(unreadMessagesCount != null ? unreadMessagesCount : 0L);

        // Add client information
        if (project.getClient() != null) {
            Client client = project.getClient();
            builder.clientId(client.getId())
                   .clientName(client.getName())
                   .clientEmail(client.getEmail())
                   .clientPhone(client.getPhone());
        }

        // Add freelancer information
        if (project.getFreelancer() != null) {
            User freelancer = project.getFreelancer();
            builder.freelancerId(freelancer.getId())
                   .freelancerName(freelancer.getFirstName() + " " + freelancer.getLastName())
                   .freelancerEmail(freelancer.getEmail());
        }

        // Add files if requested
        if (includeFiles && project.getFiles() != null) {
            builder.files(project.getFiles().stream()
                    .map(FileMapper::toResponseDto)
                    .collect(Collectors.toList()));
        } else {
            // Initialize with empty list instead of null
            builder.files(Collections.emptyList());
        }

        // Add invoices if requested
        if (includeInvoices && project.getInvoices() != null) {
            builder.invoices(project.getInvoices().stream()
                    .map(InvoiceMapper::toDto)
                    .collect(Collectors.toList()));
        } else {
            // Initialize with empty list instead of null
            builder.invoices(Collections.emptyList());
        }

        // Add messages if requested
        if (includeMessages) {
            // If messages are specifically requested but not available, initialize with empty list
            builder.messages(Collections.emptyList());
        } else {
            // Initialize with empty list instead of null
            builder.messages(Collections.emptyList());
        }

        return builder.build();
    }

    /**
     * Map Project entity to ProjectDetailDto with messages and conversations.
     *
     * @param project the project entity
     * @param messages the list of messages associated with the project
     * @param conversations the list of conversations associated with the project
     * @return the detailed project DTO
     */
    public static ProjectDetailDto toDetailDto(Project project, List<Message> messages, List<Conversation> conversations) {
        if (project == null) {
            return null;
        }
        
        // Calculate counts
        boolean includeFiles = project.getFiles() != null && !project.getFiles().isEmpty();
        boolean includeInvoices = project.getInvoices() != null && !project.getInvoices().isEmpty();
        boolean includeMessages = messages != null && !messages.isEmpty();
        
        // Calculate financial amounts
        BigDecimal invoicedAmount = BigDecimal.ZERO;
        BigDecimal paidAmount = BigDecimal.ZERO;
        BigDecimal pendingAmount = BigDecimal.ZERO;
        
        if (project.getInvoices() != null) {
            for (Invoice invoice : project.getInvoices()) {
                if (invoice.getAmount() != null) {
                    invoicedAmount = invoicedAmount.add(invoice.getAmount());
                    
                    if (invoice.getAmountPaid() != null) {
                        paidAmount = paidAmount.add(invoice.getAmountPaid());
                    }
                }
            }
            
            pendingAmount = invoicedAmount.subtract(paidAmount);
        }
        
        // Calculate unread messages count
        Long unreadMessagesCount = 0L;
        if (messages != null) {
            unreadMessagesCount = messages.stream()
                .filter(message -> !message.getIsRead() && !message.getSender().equals(project.getFreelancer()))
                .count();
        }
        
        return toDetailDto(
            project,
            includeFiles,
            includeInvoices,
            includeMessages,
            unreadMessagesCount,
            invoicedAmount,
            paidAmount,
            pendingAmount
        );
    }
    
    /**
     * Create a new Project entity from client and freelancer.
     *
     * @param projectDto the project data
     * @param client the client
     * @param freelancer the freelancer
     * @return a new project entity
     */
    public static Project toEntity(ProjectDto projectDto, Client client, User freelancer) {
        if (projectDto == null) {
            return null;
        }

        return Project.builder()
                .name(projectDto.getName())
                .description(projectDto.getDescription())
                .status(projectDto.getStatus() != null ? projectDto.getStatus() : Project.Status.PENDING)
                .startDate(projectDto.getStartDate())
                .endDate(projectDto.getEndDate())
                .budget(projectDto.getBudget())
                .hourlyRate(projectDto.getHourlyRate())
                .client(client)
                .freelancer(freelancer)
                .build();
    }

    /**
     * Update an existing Project entity from a ProjectDto.
     *
     * @param project the project entity to update
     * @param projectDto the project DTO with new values
     * @param client the client to associate (if changing)
     * @return the updated project entity
     */
    public static Project updateFromDto(Project project, ProjectDto projectDto, Client client) {
        if (project != null && projectDto != null) {
            if (projectDto.getName() != null) {
                project.setName(projectDto.getName());
            }
            
            if (projectDto.getDescription() != null) {
                project.setDescription(projectDto.getDescription());
            }
            
            if (projectDto.getStatus() != null) {
                // If changing to completed status, set completedAt timestamp
                if (project.getStatus() != Project.Status.COMPLETED && 
                    projectDto.getStatus() == Project.Status.COMPLETED) {
                    project.setCompletedAt(java.time.LocalDateTime.now());
                }
                
                project.setStatus(projectDto.getStatus());
            }
            
            if (projectDto.getStartDate() != null) {
                project.setStartDate(projectDto.getStartDate());
            }
            
            if (projectDto.getEndDate() != null) {
                project.setEndDate(projectDto.getEndDate());
            }
            
            if (projectDto.getBudget() != null) {
                project.setBudget(projectDto.getBudget());
            }
            
            if (projectDto.getHourlyRate() != null) {
                project.setHourlyRate(projectDto.getHourlyRate());
            }
            
            if (client != null) {
                project.setClient(client);
            }
        }
        
        return project;
    }
}