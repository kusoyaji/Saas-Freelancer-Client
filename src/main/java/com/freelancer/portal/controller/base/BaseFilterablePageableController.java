package com.freelancer.portal.controller.base;

import com.freelancer.portal.dto.PaginatedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Base controller that combines pagination and filtering capabilities.
 * Provides consistent response format for paginated and filtered list endpoints.
 */
public abstract class BaseFilterablePageableController implements PageableController, FilterableController {
    
    /**
     * Creates a paginated and filtered response from a repository query.
     * 
     * @param entityClass The entity class being queried
     * @param fetchFunction The function to fetch entities with specification and pageable
     * @param mapper The function to map entities to DTOs
     * @param params All request parameters for filtering
     * @param page Page number
     * @param size Page size
     * @param sortBy Field to sort by
     * @param direction Sort direction
     * @return A ResponseEntity containing a PaginatedResponse with the results
     */
    protected <T, D> ResponseEntity<PaginatedResponse<D>> getPaginatedResponse(
            Class<T> entityClass,
            BiFunction<Specification<T>, Pageable, Page<T>> fetchFunction,
            java.util.function.Function<T, D> mapper,
            @RequestParam Map<String, String> params,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        // Create pageable and specification objects
        Pageable pageable = createPageable(page, size, sortBy, direction);
        Specification<T> spec = createSpecification(params);
        
        // Fetch data using the provided function
        Page<T> resultPage = fetchFunction.apply(spec, pageable);
        
        // Transform entities to DTOs
        List<D> dtos = resultPage.getContent().stream()
                .map(mapper)
                .collect(Collectors.toList());
        
        // Create the paginated response
        PaginatedResponse<D> response = new PaginatedResponse<>(
                dtos,
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages(),
                resultPage.isFirst(),
                resultPage.isLast()
        );
        
        return ResponseEntity.ok(response);
    }
}