package com.freelancer.portal.repository.impl;

import com.freelancer.portal.model.FileMetadata;
import com.freelancer.portal.repository.FileMetadataRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * In-memory implementation of FileMetadataRepository interface.
 * This repository stores file metadata in memory as a temporary solution.
 * In a production environment, this should be replaced with a persistent storage solution.
 */
@Repository
public class FileMetadataRepositoryImpl implements FileMetadataRepository {

    private final Map<String, FileMetadata> storage = new ConcurrentHashMap<>();
    private final Map<Long, FileMetadata> idStorage = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @Override
    public FileMetadata save(FileMetadata metadata) {
        // Assign an ID if it doesn't have one
        if (metadata.getId() == null) {
            metadata.setId(idCounter.getAndIncrement());
        }
        
        // Store by filename and by ID
        storage.put(metadata.getFilename(), metadata);
        idStorage.put(metadata.getId(), metadata);
        return metadata;
    }

    @Override
    public Optional<FileMetadata> findByFilename(String filename) {
        return Optional.ofNullable(storage.get(filename));
    }

    @Override
    public List<FileMetadata> findAllByProjectId(String projectId) {
        return storage.values().stream()
                .filter(metadata -> projectId.equals(metadata.getProjectId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<FileMetadata> findAllByUserId(String userId) {
        return storage.values().stream()
                .filter(metadata -> userId.equals(metadata.getUserId()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByFilename(String filename) {
        FileMetadata metadata = storage.remove(filename);
        if (metadata != null && metadata.getId() != null) {
            idStorage.remove(metadata.getId());
        }
    }
    
    @Override
    public Collection<FileMetadata> findByEntityTypeAndEntityId(String entityType, Long entityId) {
        return storage.values().stream()
                .filter(metadata -> entityType.equals(metadata.getEntityType()) && 
                        entityId.equals(metadata.getEntityId()))
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<FileMetadata> findById(Long id) {
        return Optional.ofNullable(idStorage.get(id));
    }
    
    @Override
    public void delete(FileMetadata metadata) {
        if (metadata == null) {
            return;
        }
        
        // Remove from both maps
        if (metadata.getFilename() != null) {
            storage.remove(metadata.getFilename());
        }
        
        if (metadata.getId() != null) {
            idStorage.remove(metadata.getId());
        }
    }
}