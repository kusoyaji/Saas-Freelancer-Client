package com.freelancer.portal.controller.base;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Base interface for controllers that support pagination.
 * Provides consistent pagination and sorting support across all list endpoints.
 */
public interface PageableController {

    /**
     * Creates a Pageable object based on request parameters.
     * 
     * @param page The page number (0-based)
     * @param size The page size
     * @param sortBy The field to sort by
     * @param direction The sort direction (ASC or DESC)
     * @return A Pageable object for use with Spring Data repositories
     */
    default Pageable createPageable(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        // Validate maximum page size to prevent performance issues
        int validatedSize = Math.min(size, 100);
        
        Sort sort = Sort.by(
            direction.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC,
            sortBy
        );
        
        return PageRequest.of(page, validatedSize, sort);
    }
}