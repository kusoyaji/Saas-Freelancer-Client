package com.freelancer.portal.service.storage;

import com.freelancer.portal.exception.FileStorageException;
import com.freelancer.portal.model.FileMetadata;
import com.freelancer.portal.repository.FileMetadataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of StorageStrategy that stores files on the local filesystem.
 */
@Component
@Slf4j
public class LocalStorageStrategy implements StorageStrategy {

    private final Path fileStorageLocation;
    private final FileMetadataRepository fileMetadataRepository;
    private final String baseUrl;

    public LocalStorageStrategy(
            @Value("${file.upload-dir:uploads}") String uploadDir,
            @Value("${app.file-storage.base-url:/api/v1/files/}") String baseUrl,
            FileMetadataRepository fileMetadataRepository) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.baseUrl = baseUrl;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored", ex);
        }
    }

    @Override
    public String store(MultipartFile file, String entityType, Long entityId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Failed to store empty file");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFilename.contains("..")) {
            throw new FileStorageException(
                    "Cannot store file with relative path outside current directory " + originalFilename);
        }

        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        Path targetLocation = fileStorageLocation.resolve(uniqueFilename);

        log.info("Storing {} file for entity {}/{} as {}", 
                    entityType, entityId, originalFilename, uniqueFilename);

        // Create directory if it doesn't exist
        if (!Files.exists(fileStorageLocation)) {
            Files.createDirectories(fileStorageLocation);
        }
        
        try {
            // Save the file
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Keep file metadata in database for lookup
            FileMetadata metadata = new FileMetadata();
            metadata.setEntityType(entityType);
            metadata.setEntityId(entityId);
            metadata.setFilename(uniqueFilename);
            metadata.setOriginalFilename(originalFilename);
            metadata.setFileUrl(uniqueFilename);
            metadata.setUrl(baseUrl + uniqueFilename);  // Store full URL for direct access
            metadata.setContentType(file.getContentType());
            metadata.setMimeType(file.getContentType());
            metadata.setSize(file.getSize());
            metadata.setUploadedAt(LocalDateTime.now());
            
            fileMetadataRepository.save(metadata);
            
            log.info("Successfully stored file {} for {}/{}", uniqueFilename, entityType, entityId);
            
            // Return the URL for the file
            return metadata.getUrl();
        } catch (IOException ex) {
            log.error("Failed to store file {} for {}/{}: {}", 
                      originalFilename, entityType, entityId, ex.getMessage(), ex);
            throw new FileStorageException("Could not store file " + originalFilename, ex);
        }
    }

    @Override
    public Resource retrieve(String fileLocation) throws IOException {
        try {
            // If the fileLocation is a full URL, extract just the filename part
            String filename = fileLocation;
            if (fileLocation.startsWith(baseUrl)) {
                filename = fileLocation.substring(baseUrl.length());
            }
            
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                log.error("File not found: {}", fileLocation);
                throw new FileStorageException("File not found: " + fileLocation);
            }
        } catch (MalformedURLException ex) {
            log.error("File not found: {}", fileLocation, ex);
            throw new FileStorageException("File not found: " + fileLocation, ex);
        }
    }

    @Override
    public void delete(String fileLocation) throws IOException {
        // If the fileLocation is a full URL, extract just the filename part
        String filename = fileLocation;
        if (fileLocation.startsWith(baseUrl)) {
            filename = fileLocation.substring(baseUrl.length());
        }
        
        Path filePath = this.fileStorageLocation.resolve(filename).normalize();
        boolean deleted = Files.deleteIfExists(filePath);
        
        if (deleted) {
            log.info("Successfully deleted file: {}", filename);
        } else {
            log.warn("File not found for deletion: {}", filename);
        }
    }
}