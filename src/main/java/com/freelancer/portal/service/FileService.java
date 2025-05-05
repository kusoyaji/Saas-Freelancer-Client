package com.freelancer.portal.service;

import com.freelancer.portal.dto.FileResponseDto;
import com.freelancer.portal.model.FileMetadata;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Service interface for file operations.
 */
public interface FileService {

    /**
     * Get all files for the current user with pagination.
     *
     * @param pageable pagination information
     * @return a page of file response DTOs
     */
    Page<FileResponseDto> getAllFiles(Pageable pageable);

    /**
     * Get files for a specific project with pagination.
     *
     * @param projectId the project ID
     * @param pageable pagination information
     * @return a page of file response DTOs
     */
    Page<FileResponseDto> getFilesByProject(Long projectId, Pageable pageable);

    /**
     * Get a file by ID.
     *
     * @param id the file ID
     * @return the file response DTO
     */
    FileResponseDto getFileById(Long id);

    /**
     * Store a file for a project.
     *
     * @param file the file to store
     * @param projectId the project ID
     * @param description the file description (optional)
     * @return the created file response DTO
     * @throws IOException if an I/O error occurs
     */
    FileResponseDto storeFile(MultipartFile file, Long projectId, String description) throws IOException;

    /**
     * Update file information.
     *
     * @param id the file ID
     * @param description the new description (optional)
     * @param projectId the new project ID (optional)
     * @return the updated file response DTO
     */
    FileResponseDto updateFile(Long id, String description, Long projectId);

    /**
     * Delete a file.
     *
     * @param id the file ID
     * @throws IOException if an I/O error occurs
     */
    void deleteFile(Long id) throws IOException;

    /**
     * Load a file as a resource for downloading.
     *
     * @param id the file ID
     * @return the file resource
     * @throws IOException if an I/O error occurs
     */
    Resource loadFileAsResource(Long id) throws IOException;

    /**
     * Check if the current user is the owner of the file.
     *
     * @param id the file ID
     * @return true if the current user is the owner, false otherwise
     */
    boolean isFileOwner(Long id);
    
    /**
     * Upload a file associated with a specific entity type and ID.
     *
     * @param entityType the type of entity this file is associated with
     * @param entityId the ID of the entity this file is associated with
     * @param file the file to upload
     * @return the created file metadata
     * @throws IOException if an I/O error occurs
     */
    FileMetadata upload(String entityType, Long entityId, MultipartFile file) throws IOException;
    
    /**
     * Download a file by its metadata ID.
     *
     * @param fileId the ID of the file metadata
     * @return the file resource
     * @throws IOException if an I/O error occurs
     */
    Resource download(Long fileId) throws IOException;
    
    /**
     * Delete a file by its metadata ID.
     *
     * @param fileId the ID of the file metadata
     * @throws IOException if an I/O error occurs
     */
    void delete(Long fileId) throws IOException;
}