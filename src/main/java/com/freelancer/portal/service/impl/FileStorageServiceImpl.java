package com.freelancer.portal.service.impl;

import com.freelancer.portal.exception.FileStorageException;
import com.freelancer.portal.model.FileMetadata;
import com.freelancer.portal.repository.FileMetadataRepository;
import com.freelancer.portal.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Implementation of the FileStorageService interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.file-storage.upload-dir:uploads}")
    private String uploadDir;

    private final FileMetadataRepository fileMetadataRepository;
    private Path rootLocation;

    @PostConstruct
    @Override
    public void init() {
        rootLocation = Paths.get(uploadDir);
        try {
            Files.createDirectories(rootLocation);
            log.info("File storage initialized at: {}", rootLocation.toAbsolutePath());
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize file storage", e);
        }
    }

    @Override
    public FileMetadata store(MultipartFile file, String userId, String projectId) {
        if (file.isEmpty()) {
            throw new FileStorageException("Failed to store empty file");
        }

        try {
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            if (originalFilename.contains("..")) {
                throw new FileStorageException("Cannot store file with relative path outside current directory: " + originalFilename);
            }

            // Generate a unique filename using UUID to prevent overwrites
            String extension = "";
            if (originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID() + extension;

            // Copy the file to the storage location
            Path targetLocation = rootLocation.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Generate file URL
            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/files/")
                    .path(filename)
                    .toUriString();

            // Create and save file metadata
            FileMetadata metadata = FileMetadata.builder()
                    .filename(filename)
                    .originalFilename(originalFilename)
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .uploadedAt(LocalDateTime.now())
                    .uploadDir(uploadDir)
                    .fileUrl(fileUrl)
                    .userId(Long.valueOf(userId))
                    .projectId(Long.valueOf(projectId))
                    .build();

            fileMetadataRepository.save(metadata);
            log.info("Stored file: {} as {}", originalFilename, filename);
            return metadata;

        } catch (IOException e) {
            throw new FileStorageException("Failed to store file", e);
        }
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new FileStorageException("Could not read file: " + filename, e);
        }
    }

    @Override
    public boolean delete(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            boolean deleted = FileSystemUtils.deleteRecursively(file);
            if (deleted) {
                fileMetadataRepository.deleteByFilename(filename);
                log.info("Deleted file: {}", filename);
            }
            return deleted;
        } catch (IOException e) {
            throw new FileStorageException("Could not delete file: " + filename, e);
        }
    }

    @Override
    public Path getUploadRootDir() {
        return rootLocation;
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(rootLocation, 1)
                    .filter(path -> !path.equals(rootLocation))
                    .map(rootLocation::relativize);
        } catch (IOException e) {
            throw new FileStorageException("Failed to read stored files", e);
        }
    }

    @Override
    public String getFileBaseUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/")
                .toUriString();
    }
}