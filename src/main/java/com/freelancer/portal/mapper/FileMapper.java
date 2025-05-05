package com.freelancer.portal.mapper;

import com.freelancer.portal.dto.FileDto;
import com.freelancer.portal.dto.FileResponseDto;
import com.freelancer.portal.model.File;
import com.freelancer.portal.model.FileMetadata;

/**
 * Utility class for mapping File entities to DTOs and vice versa.
 */
public class FileMapper {

    /**
     * Maps a File entity to a FileResponseDto.
     *
     * @param file the file entity
     * @return the file response DTO
     */
    public static FileResponseDto toResponseDto(File file) {
        if (file == null) {
            return null;
        }
        
        return FileResponseDto.builder()
                .id(file.getId())
                .name(file.getName())
                .description(file.getDescription())
                .url(file.getFilePath())
                .mimeType(file.getMimeType())
                .size(file.getSize())
                .projectId(file.getProject() != null ? file.getProject().getId() : null)
                .projectName(file.getProject() != null ? file.getProject().getName() : null)
                .ownerId(file.getOwner() != null ? file.getOwner().getId() : null)
                .ownerName(file.getOwner() != null ? file.getOwner().getFirstName() + " " + file.getOwner().getLastName() : null)
                .uploadedAt(file.getUploadedAt())
                .createdAt(file.getCreatedAt())
                .updatedAt(file.getUpdatedAt())
                .build();
    }

    /**
     * Maps a File entity to a FileDto.
     *
     * @param file the file entity
     * @return the file DTO
     */
    public static FileDto toDto(File file) {
        if (file == null) {
            return null;
        }
        
        return FileDto.builder()
                .id(file.getId())
                .name(file.getName())
                .fileUrl(file.getFilePath())
                .fileType(file.getMimeType())
                .fileSize(file.getSize())
                .description(file.getDescription())
                .projectId(file.getProject() != null ? file.getProject().getId() : null)
                .uploadedById(file.getOwner() != null ? file.getOwner().getId() : null)
                .createdAt(file.getCreatedAt())
                .updatedAt(file.getUpdatedAt())
                .build();
    }

    /**
     * Maps a FileMetadata to a FileResponseDto.
     *
     * @param metadata the file metadata
     * @return the file response DTO
     */
    public static FileResponseDto fromMetadata(FileMetadata metadata) {
        if (metadata == null) {
            return null;
        }
        
        return FileResponseDto.builder()
                .id(metadata.getId())
                .name(metadata.getFilename())
                .url(metadata.getUrl() != null ? metadata.getUrl() : metadata.getFileUrl())
                .mimeType(metadata.getMimeType())
                .size(metadata.getSize())
                .projectId(metadata.getProjectId())
                .ownerId(metadata.getUserId())
                .uploadedAt(metadata.getUploadedAt())
                .createdAt(metadata.getUploadedAt())
                .build();
    }
}