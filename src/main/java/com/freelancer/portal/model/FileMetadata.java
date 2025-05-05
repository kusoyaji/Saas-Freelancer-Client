package com.freelancer.portal.model;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Class representing metadata for a file.
 * This is a lightweight class used for storage operations and does not directly map to a database entity.
 */
@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {

    private Long id;
    private String filename;
    private String originalFilename;
    private String contentType;
    private long size;
    private LocalDateTime uploadedAt;
    private String uploadDir;
    private String fileUrl;
    private String url;  // Alternative URL field
    private String entityType;  // Type of entity this file is associated with
    private Long entityId;    // ID of the entity this file is associated with
    private String mimeType;  // MIME type of the file
    private Long userId;
    private Long projectId;
    private Long contentId;
    private String contentType2;
}