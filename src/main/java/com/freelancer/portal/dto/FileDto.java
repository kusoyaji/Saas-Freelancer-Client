package com.freelancer.portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDto {
    private Long id;
    
    @NotBlank(message = "File name is required")
    private String name;
    
    private String fileUrl;
    
    private String fileType;
    
    private Long fileSize;
    
    private String description;
    
    @NotNull(message = "Project ID is required")
    private Long projectId;
    
    private Long uploadedById;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}