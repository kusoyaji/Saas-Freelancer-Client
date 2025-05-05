package com.freelancer.portal.repository;

import com.freelancer.portal.model.FileMetadata;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for file metadata operations.
 * This is not a JPA repository but an interface for storing and retrieving file metadata.
 */
@Repository
public interface FileMetadataRepository {

    /**
     * Save file metadata.
     *
     * @param metadata the file metadata
     * @return the saved file metadata
     */
    FileMetadata save(FileMetadata metadata);

    /**
     * Find file metadata by filename.
     *
     * @param filename the filename
     * @return an optional containing the file metadata if found, empty otherwise
     */
    Optional<FileMetadata> findByFilename(String filename);

    /**
     * Find all file metadata by project ID.
     *
     * @param projectId the project ID
     * @return a list of file metadata
     */
    List<FileMetadata> findAllByProjectId(String projectId);

    /**
     * Find all file metadata by user ID.
     *
     * @param userId the user ID
     * @return a list of file metadata
     */
    List<FileMetadata> findAllByUserId(String userId);

    /**
     * Delete file metadata by filename.
     *
     * @param filename the filename
     */
    void deleteByFilename(String filename);

    /**
     * Find file metadata by entity type and entity ID.
     * 
     * @param entityType the type of entity
     * @param entityId the ID of the entity
     * @return a collection of file metadata
     */
    Collection<FileMetadata> findByEntityTypeAndEntityId(String entityType, Long entityId);

    /**
     * Find file metadata by ID.
     * 
     * @param id the file metadata ID
     * @return an optional containing the file metadata if found, empty otherwise
     */
    Optional<FileMetadata> findById(Long id);
    
    /**
     * Delete file metadata.
     * 
     * @param metadata the file metadata to delete
     */
    void delete(FileMetadata metadata);
}