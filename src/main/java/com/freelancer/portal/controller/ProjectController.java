package com.freelancer.portal.controller;

import com.freelancer.portal.dto.ProjectDto;
import com.freelancer.portal.dto.ProjectDetailDto;
import com.freelancer.portal.dto.ProjectRequestDto;
import com.freelancer.portal.model.Project;
import com.freelancer.portal.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for project management operations.
 */
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    
    /**
     * Get all projects for the current user with pagination.
     * Optional parameter to include related entities (files, invoices, etc.)
     */
    @GetMapping
    public ResponseEntity<Page<ProjectDto>> getAllProjects(
            Pageable pageable,
            @RequestParam(defaultValue = "false") boolean includeRelated) {
        return ResponseEntity.ok(projectService.getAllProjects(pageable, includeRelated));
    }
    
    /**
     * Get projects for a specific client with pagination.
     * Only accessible if the client belongs to the current user.
     * Optional parameter to include related entities (files, invoices, etc.)
     */
    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<Page<ProjectDto>> getProjectsByClient(
            @PathVariable Long clientId, 
            Pageable pageable,
            @RequestParam(defaultValue = "false") boolean includeRelated) {
        return ResponseEntity.ok(projectService.getProjectsByClient(clientId, pageable, includeRelated));
    }
    
    /**
     * Get a project by ID.
     * Only accessible if the project belongs to the current user.
     * Optional parameter to include related entities (files, invoices, etc.)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<ProjectDetailDto> getProjectById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean includeRelated) {
        return ResponseEntity.ok(projectService.getProjectById(id, includeRelated));
    }
    
    /**
     * Create a new project.
     * Optional parameter to include related entities in the response.
     */
    @PostMapping
    public ResponseEntity<ProjectDetailDto> createProject(
            @Valid @RequestBody ProjectRequestDto projectRequestDto,
            @RequestParam(defaultValue = "false") boolean includeRelated) {
        return new ResponseEntity<>(projectService.createProject(projectRequestDto, includeRelated), HttpStatus.CREATED);
    }
    
    /**
     * Update an existing project.
     * Only accessible if the project belongs to the current user.
     * Optional parameter to include related entities in the response.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<ProjectDetailDto> updateProject(
            @PathVariable Long id, 
            @Valid @RequestBody ProjectDto projectRequestDto,
            @RequestParam(defaultValue = "false") boolean includeRelated) {
        return ResponseEntity.ok(projectService.updateProject(id, projectRequestDto, includeRelated));
    }
    
    /**
     * Update project status.
     * Only accessible if the project belongs to the current user.
     * Optional parameter to include related entities in the response.
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<ProjectDetailDto> updateProjectStatus(
            @PathVariable Long id, 
            @RequestParam Project.Status status,
            @RequestParam(defaultValue = "false") boolean includeRelated) {
        return ResponseEntity.ok(projectService.changeStatus(id, status, includeRelated));
    }
    
    /**
     * Delete a project.
     * Only accessible if the project belongs to the current user.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}