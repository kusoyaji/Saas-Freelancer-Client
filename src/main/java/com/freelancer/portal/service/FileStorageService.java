package com.freelancer.portal.service;

import com.freelancer.portal.model.FileMetadata;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Service interface for file storage operations.
 */
public interface FileStorageService {

    /**
     * Initialize the file storage.
     */
    void init();

    /**
     * Store a file and return its metadata.
     *
     * @param file the file to store
     * @param userId the ID of the user who uploaded the file
     * @param projectId the ID of the project related to the file (optional)
     * @return the stored file's metadata
     */
    FileMetadata store(MultipartFile file, String userId, String projectId);

    /**
     * Load a file as a resource.
     *
     * @param filename the name of the file to load
     * @return the file as a resource
     */
    Resource loadAsResource(String filename);

    /**
     * Delete a file.
     *
     * @param filename the name of the file to delete
     * @return true if the file was deleted, false otherwise
     */
    boolean delete(String filename);

    /**
     * Get the root directory for file uploads.
     *
     * @return the root directory as a Path
     */
    Path getUploadRootDir();

    /**
     * Load all stored files as a stream of paths.
     *
     * @return a stream of paths to all stored files
     */
    Stream<Path> loadAll();

    /**
     * Get the base URL for file access.
     *
     * @return the base URL as a string
     */
    String getFileBaseUrl();
}