package com.freelancer.portal.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Interface for file storage strategies.
 * Implementations can store files in different locations (local disk, cloud storage, etc.)
 */
public interface StorageStrategy {

    /**
     * Store a file.
     *
     * @param file the file to store
     * @param entityType the type of entity this file is associated with
     * @param entityId the ID of the entity this file is associated with
     * @return the URL or path where the file is stored
     * @throws IOException if an I/O error occurs
     */
    String store(MultipartFile file, String entityType, Long entityId) throws IOException;

    /**
     * Retrieve a file.
     *
     * @param fileUrl the URL or path where the file is stored
     * @return the file resource
     * @throws IOException if an I/O error occurs
     */
    Resource retrieve(String fileUrl) throws IOException;

    /**
     * Delete a file.
     *
     * @param fileUrl the URL or path where the file is stored
     * @throws IOException if an I/O error occurs
     */
    void delete(String fileUrl) throws IOException;
}