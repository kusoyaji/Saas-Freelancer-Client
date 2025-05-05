package com.freelancer.portal.controller;

import com.freelancer.portal.dto.FileResponseDto;
import com.freelancer.portal.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileService fileService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<FileResponseDto> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("projectId") Long projectId,
            @RequestParam(value = "description", required = false) String description) throws IOException {

        FileResponseDto responseDto = fileService.storeFile(file, projectId, description);
        return ResponseEntity.ok(responseDto);
    }
}