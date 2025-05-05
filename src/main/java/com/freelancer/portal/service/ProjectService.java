package com.freelancer.portal.service;

import com.freelancer.portal.dto.ProjectDetailDto;
import com.freelancer.portal.dto.ProjectDto;
import com.freelancer.portal.dto.ProjectRequestDto;
import com.freelancer.portal.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service interface for project operations.
 */
public interface ProjectService {

    /**
     * Get all projects for the current freelancer with pagination.
     *
     * @param pageable pagination information
     * @return a page of project DTOs
     */
    Page<ProjectDto> getAllProjects(Pageable pageable);

    @Transactional(readOnly = true)
    Page<ProjectDetailDto> getAllProjectsWithDetails(Pageable pageable, boolean includeRelated);

    /**
     * Get all projects for the current freelancer with pagination and option to include related entities.
     *
     * @param pageable pagination information
     * @param includeRelated whether to include related entities (files, invoices, etc.)
     * @return a page of project DTOs with related entities if requested
     */
    Page<ProjectDto> getAllProjects(Pageable pageable, boolean includeRelated);

    /**
     * Get all projects for a client.
     *
     * @param clientId the client ID
     * @param pageable pagination information
     * @return a page of project DTOs
     */
    Page<ProjectDto> getProjectsByClient(Long clientId, Pageable pageable);
    
    /**
     * Get all projects for a client with option to include related entities.
     *
     * @param clientId the client ID
     * @param pageable pagination information
     * @param includeRelated whether to include related entities (files, invoices, etc.)
     * @return a page of project DTOs with related entities if requested
     */
    Page<ProjectDto> getProjectsByClient(Long clientId, Pageable pageable, boolean includeRelated);

    /**
     * Get a project by ID.
     *
     * @param id the project ID
     * @return the project detail DTO
     */
    ProjectDetailDto getProjectById(Long id);
    
    /**
     * Get a project by ID with option to include related entities.
     *
     * @param id the project ID
     * @param includeRelated whether to include related entities (files, invoices, etc.)
     * @return the project detail DTO with related entities if requested
     */
    ProjectDetailDto getProjectById(Long id, boolean includeRelated);

    /**
     * Create a new project.
     *
     * @param projectRequest the project request DTO
     * @return the created project detail DTO
     */
    ProjectDetailDto createProject(ProjectRequestDto projectRequest);
    
    /**
     * Create a new project with option to include related entities in the response.
     *
     * @param projectRequest the project request DTO
     * @param includeRelated whether to include related entities in the response
     * @return the created project detail DTO with related entities if requested
     */
    ProjectDetailDto createProject(ProjectRequestDto projectRequest, boolean includeRelated);

    /**
     * Update a project.
     *
     * @param id the project ID
     * @param projectRequest the project request DTO
     * @return the updated project detail DTO
     */
    ProjectDetailDto updateProject(Long id, ProjectDto projectRequest);
    
    /**
     * Update a project with option to include related entities in the response.
     *
     * @param id the project ID
     * @param projectRequest the project request DTO
     * @param includeRelated whether to include related entities in the response
     * @return the updated project detail DTO with related entities if requested
     */
    ProjectDetailDto updateProject(Long id, ProjectDto projectRequest, boolean includeRelated);

    /**
     * Delete a project.
     *
     * @param id the project ID
     */
    void deleteProject(Long id);

    /**
     * Change the status of a project.
     *
     * @param id the project ID
     * @param status the new status
     * @return the updated project detail DTO
     */
    ProjectDetailDto changeStatus(Long id, Project.Status status);
    
    /**
     * Change the status of a project with option to include related entities in the response.
     *
     * @param id the project ID
     * @param status the new status
     * @param includeRelated whether to include related entities in the response
     * @return the updated project detail DTO with related entities if requested
     */
    ProjectDetailDto changeStatus(Long id, Project.Status status, boolean includeRelated);

    /**
     * Check if the current user is the owner of the project.
     *
     * @param id the project ID
     * @return true if the current user is the owner, false otherwise
     */
    boolean isProjectOwner(Long id);
    
    /**
     * Get all projects for the current freelancer.
     *
     * @return a list of all project DTOs
     */
    List<ProjectDto> getAllProjectsList();
    
    /**
     * Get all projects for the current freelancer with option to include related entities.
     *
     * @param includeRelated whether to include related entities (files, invoices, etc.)
     * @return a list of all project DTOs with related entities if requested
     */
    List<ProjectDto> getAllProjectsList(boolean includeRelated);

}