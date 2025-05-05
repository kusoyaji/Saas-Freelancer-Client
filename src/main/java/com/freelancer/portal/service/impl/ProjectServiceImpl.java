package com.freelancer.portal.service.impl;

import com.freelancer.portal.dto.*;
import com.freelancer.portal.mapper.ProjectMapper;
import com.freelancer.portal.model.Client;
import com.freelancer.portal.model.Conversation;
import com.freelancer.portal.model.File;
import com.freelancer.portal.model.Invoice;
import com.freelancer.portal.model.Message;
import com.freelancer.portal.model.Notification.NotificationType;
import com.freelancer.portal.model.Project;
import com.freelancer.portal.model.User;
import com.freelancer.portal.repository.ClientRepository;
import com.freelancer.portal.repository.ConversationRepository;
import com.freelancer.portal.repository.FileRepository;
import com.freelancer.portal.repository.InvoiceRepository;
import com.freelancer.portal.repository.MessageRepository;
import com.freelancer.portal.repository.ProjectRepository;
import com.freelancer.portal.repository.UserRepository;
import com.freelancer.portal.service.NotificationService;
import com.freelancer.portal.service.ProjectService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final InvoiceRepository invoiceRepository;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDto> getAllProjects(Pageable pageable) {
        return getAllProjects(pageable, false);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDetailDto> getAllProjectsWithDetails(Pageable pageable, boolean includeRelated) {
        User currentUser = getCurrentUser();

        // First fetch the projects
        Page<Project> projects = projectRepository.findByFreelancer(currentUser, pageable);

        if (includeRelated) {
            // For each project, explicitly load files and invoices
            for (Project project : projects.getContent()) {
                // Load files using the helper method
                List<File> files = fileRepository.findByProjectId(project.getId());
                project.clearFiles();
                for (File file : files) {
                    project.addFile(file);
                }

                // Load invoices with payment information
                List<Invoice> invoices = invoiceRepository.findByProjectId(project.getId());
                project.getInvoices().clear();     // Clear the existing collection
                if (invoices != null) {
                    project.getInvoices().addAll(invoices); // Add all new items
                }

                // Load messages if repository method exists
                loadMessagesAndConversationsForProject(project);
            }
        }

        // Map to DTO with loaded entities
        return projects.map(project -> {
            if (includeRelated) {
                List<Message> messages = messageRepository.findByProjectOrderByCreatedAtDesc(project);
                List<Conversation> conversations = conversationRepository.findAll().stream()
                        .filter(conv -> conv.getProject() != null && conv.getProject().getId().equals(project.getId()))
                        .collect(Collectors.toList());

                return ProjectMapper.toDetailDto(project, messages, conversations);
            } else {
                return ProjectMapper.toDetailDto(project, null, null);
            }
        });
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDto> getAllProjects(Pageable pageable, boolean includeRelated) {
        User currentUser = getCurrentUser();
        
        return projectRepository.findByFreelancer(currentUser, pageable)
                .map(project -> {
                    if (includeRelated) {
                        // Load necessary related entities to calculate counts and amounts
                        loadRelatedEntitiesForProject(project);
                        
                        // Calculate counts and amounts
                        Integer filesCount = project.getFiles() != null ? project.getFiles().size() : 0;
                        Integer invoicesCount = project.getInvoices() != null ? project.getInvoices().size() : 0;
                        
                        // Count messages and conversations
                        Integer messagesCount = (int) messageRepository.countByProject(project);
                        Integer conversationsCount = (int) conversationRepository.countByProject(project);
                        Integer unreadMessagesCount = (int) messageRepository.countByProjectAndIsReadFalseAndSenderNot(project, currentUser);
                        
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
                        
                        return ProjectMapper.toDto(
                            project,
                            filesCount,
                            invoicesCount,
                            messagesCount,
                            conversationsCount,
                            unreadMessagesCount,
                            invoicedAmount,
                            paidAmount,
                            pendingAmount,
                            null // overduedInvoicesCount - we can calculate this if needed
                        );
                    } else {
                        // For basic listing, initialize counts to 0 rather than null
                        return ProjectMapper.toDto(
                            project,
                            0, // filesCount
                            0, // invoicesCount
                            0, // messagesCount
                            0, // conversationsCount
                            0, // unreadMessagesCount
                            BigDecimal.ZERO, // invoicedAmount
                            BigDecimal.ZERO, // paidAmount
                            BigDecimal.ZERO, // pendingAmount
                            0L  // overduedInvoicesCount
                        );
                    }
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDto> getProjectsByClient(Long clientId, Pageable pageable) {
        return getProjectsByClient(clientId, pageable, false);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDto> getProjectsByClient(Long clientId, Pageable pageable, boolean includeRelated) {
        User currentUser = getCurrentUser();
        Client client = clientRepository.findByIdAndFreelancer(clientId, currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + clientId));
        
        return projectRepository.findByClient(client, pageable)
                .map(project -> {
                    if (includeRelated) {
                        // Load necessary related entities to calculate counts and amounts
                        loadRelatedEntitiesForProject(project);
                        
                        // Calculate counts and amounts (mirroring getAllProjects logic)
                        Integer filesCount = project.getFiles() != null ? project.getFiles().size() : 0;
                        Integer invoicesCount = project.getInvoices() != null ? project.getInvoices().size() : 0;
                        Integer messagesCount = (int) messageRepository.countByProject(project);
                        Integer conversationsCount = (int) conversationRepository.countByProject(project);
                        Integer unreadMessagesCount = (int) messageRepository.countByProjectAndIsReadFalseAndSenderNot(project, currentUser);
                        
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
                        
                        // Call the overloaded mapper with calculated counts
                        return ProjectMapper.toDto(
                            project,
                            filesCount,
                            invoicesCount,
                            messagesCount,
                            conversationsCount,
                            unreadMessagesCount,
                            invoicedAmount,
                            paidAmount,
                            pendingAmount,
                            null // overduedInvoicesCount
                        );
                    } else {
                        // For basic listing, call the overloaded mapper with 0 counts
                        return ProjectMapper.toDto(
                            project,
                            0, // filesCount
                            0, // invoicesCount
                            0, // messagesCount
                            0, // conversationsCount
                            0, // unreadMessagesCount
                            BigDecimal.ZERO, // invoicedAmount
                            BigDecimal.ZERO, // paidAmount
                            BigDecimal.ZERO, // pendingAmount
                            0L  // overduedInvoicesCount
                        );
                    }
                });
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDetailDto getProjectById(Long id) {
        return getProjectById(id, true);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDetailDto getProjectById(Long id, boolean includeRelated) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findByIdAndFreelancer(id, currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));

        if (includeRelated) {
            loadRelatedEntitiesForProject(project);
            
            // Load messages and conversations specifically for detail view
            List<Message> messages = messageRepository.findByProjectOrderByCreatedAtDesc(project);
            List<Conversation> conversations = conversationRepository.findAll().stream()
                    .filter(conv -> conv.getProject() != null && conv.getProject().getId().equals(project.getId()))
                    .collect(Collectors.toList());
            
            return ProjectMapper.toDetailDto(project, messages, conversations);
        } else {
            return ProjectMapper.toDetailDto(project, null, null);
        }
    }

    @Override
    @Transactional
    public ProjectDetailDto createProject(ProjectRequestDto projectRequest) {
        return createProject(projectRequest, true);
    }

    @Override
    @Transactional
    public ProjectDetailDto createProject(ProjectRequestDto projectRequest, boolean includeRelated) {
        User currentUser = getCurrentUser();

        Client client = clientRepository.findByIdAndFreelancer(projectRequest.getClientId(), currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + projectRequest.getClientId()));

        Project project = Project.builder()
                .name(projectRequest.getName())
                .description(projectRequest.getDescription())
                .budget(projectRequest.getBudget())
                .hourlyRate(projectRequest.getHourlyRate())
                .startDate(projectRequest.getStartDate())
                .endDate(projectRequest.getEndDate())
                .status(projectRequest.getStatus() != null ? projectRequest.getStatus() : Project.Status.PENDING)
                .client(client)
                .freelancer(currentUser)
                .build();

        // Save the project
        Project savedProject = projectRepository.save(project);
        
        // Create notification for project creation
        notificationService.createProjectNotification(savedProject, NotificationType.PROJECT_CREATED);

        if (includeRelated) {
            // No need to load related entities for a new project, but we can initialize them
            return ProjectMapper.toDetailDto(savedProject, List.of(), List.of());
        } else {
            return ProjectMapper.toDetailDto(savedProject, null, null);
        }
    }

    @Override
    @Transactional
    public ProjectDetailDto updateProject(Long id, ProjectDto projectRequest) {
        return updateProject(id, projectRequest, true);
    }


    @Override
    @Transactional
    public ProjectDetailDto updateProject(Long id, ProjectDto projectRequest, boolean includeRelated) {
        User currentUser = getCurrentUser();
        Project existingProject = projectRepository.findByIdAndFreelancer(id, currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));

        // Check if status is changing
        boolean statusChanged = projectRequest.getStatus() != null && 
                               !Project.Status.valueOf(projectRequest.getStatus().toString()).equals(existingProject.getStatus());

        // Fetch the client only if clientId is provided in the request
        Client client = null; // Initialize client to null
        if (projectRequest.getClientId() != null) { 
            client = clientRepository.findByIdAndFreelancer(projectRequest.getClientId(), currentUser)
                    .orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + projectRequest.getClientId()));
        } else {
            // If clientId is null in the request, we might want to explicitly set the project's client to null
            // or keep the existing one. Assuming the intent is to potentially clear the client link,
            // we leave 'client' as null. The mapper should handle this.
        }

        // Use the mapper's update method, passing the potentially null client
        final Project updatedProject = projectRepository.save(
                ProjectMapper.updateFromDto(existingProject, projectRequest, client)
        );

        // Create notification for project update
        notificationService.createProjectNotification(updatedProject, NotificationType.PROJECT_UPDATED);
        
        // If project is marked as completed, create a specific notification
        if (statusChanged && Project.Status.COMPLETED.equals(updatedProject.getStatus())) {
            notificationService.createProjectNotification(updatedProject, NotificationType.PROJECT_COMPLETED);
        }

        if (includeRelated) {
            loadRelatedEntitiesForProject(updatedProject);
            List<Message> messages = messageRepository.findByProjectOrderByCreatedAtDesc(updatedProject);

            // Now updatedProject is effectively final and can be used in the lambda
            List<Conversation> conversations = conversationRepository.findAll().stream()
                    .filter(conv -> conv.getProject() != null && conv.getProject().getId().equals(updatedProject.getId()))
                    .collect(Collectors.toList());

            return ProjectMapper.toDetailDto(updatedProject, messages, conversations);
        } else {
            return ProjectMapper.toDetailDto(updatedProject, null, null);
        }
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findByIdAndFreelancer(id, currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));
        
        projectRepository.delete(project);
    }
    
    @Override
    @Transactional
    public ProjectDetailDto changeStatus(Long id, Project.Status status) {
        return changeStatus(id, status, false);
    }
    
    @Override
    @Transactional
    public ProjectDetailDto changeStatus(Long id, Project.Status status, boolean includeRelated) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findByIdAndFreelancer(id, currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));
        
        project.setStatus(status);
        
        // If status is COMPLETED, set the completion date
        if (status == Project.Status.COMPLETED) {
            project.setCompletedAt(LocalDateTime.now());
        }
        
        Project updatedProject = projectRepository.save(project);
        
        if (includeRelated) {
            loadRelatedEntitiesForProject(updatedProject);
            List<Message> messages = messageRepository.findByProjectOrderByCreatedAtDesc(updatedProject);
            List<Conversation> conversations = conversationRepository.findAll().stream()
                    .filter(conv -> conv.getProject() != null && conv.getProject().getId().equals(updatedProject.getId()))
                    .collect(Collectors.toList());
            
            return ProjectMapper.toDetailDto(updatedProject, messages, conversations);
        } else {
            return ProjectMapper.toDetailDto(updatedProject, null, null);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isProjectOwner(Long id) {
        User currentUser = getCurrentUser();
        return projectRepository.existsByIdAndFreelancer(id, currentUser);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProjectDto> getAllProjectsList() {
        return getAllProjectsList(false);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProjectDto> getAllProjectsList(boolean includeRelated) {
        User currentUser = getCurrentUser();
        List<Project> projects = projectRepository.findByFreelancer(currentUser);
        
        if (includeRelated) {
            for (Project project : projects) {
                loadRelatedEntitiesForProject(project);
            }
        }
        
        return projects.stream()
            .map(ProjectMapper::toDto)
            .collect(Collectors.toList());
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
    
    /**
     * Helper method to load all related entities for a project
     */
    private void loadRelatedEntitiesForProject(Project project) {
        // Load files
        List<File> files = fileRepository.findByProjectId(project.getId());
        
        // Clear existing files and add each one properly to maintain bidirectional relationship
        project.clearFiles();
        for (File file : files) {
            project.addFile(file);
        }
        
        // Load invoices
        List<Invoice> invoices = invoiceRepository.findByProjectId(project.getId());
        project.getInvoices().clear();     // Clear the existing collection
        if (invoices != null) {
            project.getInvoices().addAll(invoices); // Add all new items
        }
        // Load messages and conversations if needed
        loadMessagesAndConversationsForProject(project);
    }
    
    /**
     * Helper method to specifically load messages and conversations for a project
     */
    private void loadMessagesAndConversationsForProject(Project project) {
        User currentUser = getCurrentUser();
        
        // Find conversations related to this project
        List<Conversation> conversations = conversationRepository.findByProject(project);
        
        // Find messages related to this project
        List<Message> messages = messageRepository.findByProjectOrderByCreatedAtDesc(project);
        
        // Get counts for different types of messages
        long messagesCount = messageRepository.countByProject(project);
        long conversationsCount = conversationRepository.countByProject(project);
        long unreadMessagesCount = messageRepository.countByProjectAndIsReadFalseAndSenderNot(project, currentUser);
        
        // We could add custom fields to Project entity to store these counts temporarily if needed
        // For now, we're passing these values directly to the mapper when needed
    }
}