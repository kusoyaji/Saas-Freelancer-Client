package com.freelancer.portal.controller;

import com.freelancer.portal.dto.FileResponseDto;
import com.freelancer.portal.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * REST Controller for file operations.
 */
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    
    /**
     * Get all files for the current user with pagination.
     */
    @GetMapping
    public ResponseEntity<Page<FileResponseDto>> getAllFiles(Pageable pageable) {
        return ResponseEntity.ok(fileService.getAllFiles(pageable));
    }
    
    /**
     * Get files for a specific project with pagination.
     * Only accessible if the project belongs to the current user.
     */
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<Page<FileResponseDto>> getFilesByProject(
            @PathVariable Long projectId, Pageable pageable) {
        return ResponseEntity.ok(fileService.getFilesByProject(projectId, pageable));
    }
    
    /**
     * Get a file by ID.
     * Only accessible if the file belongs to the current user.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<FileResponseDto> getFileById(@PathVariable Long id) {
        return ResponseEntity.ok(fileService.getFileById(id));
    }
    
    /**
     * Download a file by ID.
     * Only accessible if the file belongs to the current user.
     */
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) throws IOException {
        Resource resource = fileService.loadFileAsResource(id);
        String contentType = "application/octet-stream";
        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource);
    }
    
    /**
     * Upload a new file to a project.
     */
    @PostMapping("/upload")
    public ResponseEntity<FileResponseDto> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("projectId") Long projectId,
            @RequestParam(value = "description", required = false) String description) throws IOException {
        
        FileResponseDto uploadedFile = fileService.storeFile(file, projectId, description);
        return new ResponseEntity<>(uploadedFile, HttpStatus.CREATED);
    }
    
    /**
     * Update file information.
     * Only accessible if the file belongs to the current user.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<FileResponseDto> updateFile(
            @PathVariable Long id,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "projectId", required = false) Long projectId) {
        
        return ResponseEntity.ok(fileService.updateFile(id, description, projectId));
    }
    
    /**
     * Delete a file.
     * Only accessible if the file belongs to the current user.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) throws IOException {
        fileService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }
}