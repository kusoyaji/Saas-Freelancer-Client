package com.freelancer.portal.service.impl;

import com.freelancer.portal.dto.FileResponseDto;
import com.freelancer.portal.exception.FileStorageException;
import com.freelancer.portal.exception.ResourceNotFoundException;
import com.freelancer.portal.mapper.FileMapper;
import com.freelancer.portal.model.File;
import com.freelancer.portal.model.FileMetadata;
import com.freelancer.portal.model.Project;
import com.freelancer.portal.model.User;
import com.freelancer.portal.repository.FileMetadataRepository;
import com.freelancer.portal.repository.FileRepository;
import com.freelancer.portal.repository.ProjectRepository;
import com.freelancer.portal.security.SecurityUtils;
import com.freelancer.portal.service.FileService;
import com.freelancer.portal.service.storage.StorageStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private final FileRepository fileRepository;
    private final ProjectRepository projectRepository;
    private final SecurityUtils securityUtils;
    private final FileMetadataRepository fileMetadataRepository;
    private final StorageStrategy storageStrategy;

    @Override
    @Transactional(readOnly = true)
    public Page<FileResponseDto> getAllFiles(Pageable pageable) {
        User currentUser = securityUtils.getCurrentUser();
        Page<File> files = fileRepository.findAllByOwner(currentUser, pageable);
        return files.map(FileMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FileResponseDto> getFilesByProject(Long projectId, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        
        Page<File> files = fileRepository.findAllByProject(project, pageable);
        return files.map(FileMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public FileResponseDto getFileById(Long id) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));
        
        if (!isFileOwner(id)) {
            throw new ResourceNotFoundException("File not found with id: " + id);
        }
        
        return FileMapper.toResponseDto(file);
    }

    @Override
    @Transactional
    public FileResponseDto storeFile(MultipartFile multipartFile, Long projectId, String description) throws IOException {
        log.info("Attempting to store file '{}' for project ID: {}", multipartFile.getOriginalFilename(), projectId);

        if (multipartFile.isEmpty()) {
            log.error("File upload failed: Cannot store empty file.");
            throw new FileStorageException("Cannot store empty file");
        }

        // Get current user
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            log.error("File upload failed: Could not determine current user.");
            // Consider throwing AuthenticationCredentialsNotFoundException or similar
            throw new FileStorageException("User not authenticated"); 
        }
        log.debug("Current user ID: {}", currentUser.getId());

        // Check if project exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.error("File upload failed: Project not found with id: {}", projectId);
                    return new ResourceNotFoundException("Project not found with id: " + projectId);
                });
        log.debug("Found project ID: {}", project.getId());

        // --- Robust Ownership Check ---
        if (project.getFreelancer() == null) {
             log.error("File upload failed: Project ID {} has no associated freelancer.", projectId);
             throw new FileStorageException("Project owner information is missing.");
        }
        if (!project.getFreelancer().getId().equals(currentUser.getId())) {
             log.error("File upload failed: User {} does not own project {}", currentUser.getId(), projectId);
             // Use AccessDeniedException for authorization failures
             throw new AccessDeniedException("User does not have permission to upload files to this project.");
        }
        log.debug("User {} confirmed as owner of project {}", currentUser.getId(), projectId);
        // --- End Ownership Check ---

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        String storedFilename = UUID.randomUUID() + "_" + originalFilename;
        log.debug("Storing file as: {}", storedFilename);

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        log.debug("Upload directory: {}", uploadPath);

        try {
            // Create uploads directory if it doesn't exist
            if (!Files.exists(uploadPath)) {
                log.info("Creating upload directory: {}", uploadPath);
                Files.createDirectories(uploadPath);
            }

            // Copy file to the target location
            Path targetLocation = uploadPath.resolve(storedFilename);
            log.debug("Copying file to: {}", targetLocation);
            Files.copy(multipartFile.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("File successfully copied to {}", targetLocation);

        } catch (IOException ex) {
            log.error("Failed to store file {}: {}", originalFilename, ex.getMessage(), ex);
            throw new FileStorageException("Failed to store file " + originalFilename, ex);
        }

        // Create and save file metadata
        File file = new File();
        file.setName(originalFilename);
        file.setFilePath(storedFilename); // Store the relative path/filename
        file.setMimeType(multipartFile.getContentType());
        file.setSize(multipartFile.getSize());
        file.setUploadedAt(LocalDateTime.now());
        file.setDescription(description);
        file.setProject(project);
        file.setOwner(currentUser);

        try {
            File savedFile = fileRepository.save(file);
            log.info("File metadata saved successfully for file ID: {}", savedFile.getId());
            return FileMapper.toResponseDto(savedFile);
        } catch (Exception e) {
             log.error("Failed to save file metadata for {}: {}", originalFilename, e.getMessage(), e);
             // Attempt to delete the orphaned file from storage if DB save fails
             try {
                 Path targetLocation = uploadPath.resolve(storedFilename);
                 Files.deleteIfExists(targetLocation);
                 log.warn("Cleaned up orphaned file: {}", targetLocation);
             } catch (IOException cleanupEx) {
                 log.error("Failed to cleanup orphaned file {}: {}", storedFilename, cleanupEx.getMessage(), cleanupEx);
             }
             throw new FileStorageException("Failed to save file metadata.", e);
        }
    }

    @Override
    @Transactional
    public FileResponseDto updateFile(Long id, String description, Long projectId) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));
        
        if (!isFileOwner(id)) {
            throw new ResourceNotFoundException("File not found with id: " + id);
        }
        
        // Update description if provided
        if (description != null) {
            file.setDescription(description);
        }
        
        // Update project if provided
        if (projectId != null && !projectId.equals(file.getProject().getId())) {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
            
            User currentUser = securityUtils.getCurrentUser();
            if (!project.getFreelancer().getId().equals(currentUser.getId())) {
                throw new ResourceNotFoundException("Project not found with id: " + projectId);
            }
            
            file.setProject(project);
        }
        
        File updatedFile = fileRepository.save(file);
        return FileMapper.toResponseDto(updatedFile);
    }

    @Override
    @Transactional
    public void deleteFile(Long id) throws IOException {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));
        
        if (!isFileOwner(id)) {
            throw new ResourceNotFoundException("File not found with id: " + id);
        }
        
        // Delete file from filesystem
        Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(file.getFilePath());
        Files.deleteIfExists(filePath);
        
        // Delete file metadata from database
        fileRepository.delete(file);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource loadFileAsResource(Long id) throws IOException {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));
        
        if (!isFileOwner(id)) {
            throw new ResourceNotFoundException("File not found with id: " + id);
        }
        
        try {
            Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(file.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileStorageException("File not found: " + file.getName());
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("File not found: " + file.getName(), ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFileOwner(Long id) {
        User currentUser = securityUtils.getCurrentUser();
        File file = fileRepository.findById(id).orElse(null);
        
        if (file == null) {
            return false;
        }
        
        return file.getOwner().getId().equals(currentUser.getId());
    }
    
    @Override
    @Transactional
    public FileMetadata upload(String entityType, Long entityId, MultipartFile file) throws IOException {
        // Use the storage strategy to store the file
        String fileUrl = storageStrategy.store(file, entityType, entityId);
        
        // Create and save file metadata
        FileMetadata metadata = new FileMetadata();
        metadata.setEntityType(entityType);
        metadata.setEntityId(entityId);
        metadata.setFilename(file.getOriginalFilename());
        metadata.setUrl(fileUrl);
        metadata.setFileUrl(fileUrl);  // Set both URL fields for compatibility
        metadata.setSize(file.getSize());
        metadata.setMimeType(file.getContentType());
        metadata.setContentType(file.getContentType());  // Set both content type fields for compatibility
        metadata.setUploadedAt(LocalDateTime.now());
        
        // If we're uploading to a project, try to associate the metadata with the project and user
        if ("project".equalsIgnoreCase(entityType)) {
            User currentUser = securityUtils.getCurrentUser();
            metadata.setUserId(currentUser.getId());
            metadata.setProjectId(entityId);
        }
        
        return fileMetadataRepository.save(metadata);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Resource download(Long fileId) throws IOException {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File metadata not found with id: " + fileId));
        
        // Use the storage strategy to retrieve the file
        // Try url first, fall back to fileUrl if url is null
        String fileLocation = metadata.getUrl() != null ? metadata.getUrl() : metadata.getFileUrl();
        return storageStrategy.retrieve(fileLocation);
    }
    
    @Override
    @Transactional
    public void delete(Long fileId) throws IOException {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File metadata not found with id: " + fileId));
        
        // Use the storage strategy to delete the file
        // Try url first, fall back to fileUrl if url is null
        String fileLocation = metadata.getUrl() != null ? metadata.getUrl() : metadata.getFileUrl();
        storageStrategy.delete(fileLocation);
        
        // Delete the metadata
        fileMetadataRepository.delete(metadata);
    }
}