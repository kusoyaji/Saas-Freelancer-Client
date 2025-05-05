package com.freelancer.portal.controller.base;

import com.freelancer.portal.specification.SpecificationBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * Base interface for controllers that support filtering.
 * Provides consistent filtering support across all list endpoints.
 */
public interface FilterableController {

    /**
     * Creates a Specification object based on filter parameters.
     * This method extracts all filter parameters from the request
     * and builds a specification for dynamic filtering.
     * 
     * @param <T> The entity type for the specification
     * @param params The request parameters containing filter criteria
     * @return A Specification object for use with Spring Data JPA
     */
    default <T> Specification<T> createSpecification(
            @RequestParam(required = false) Map<String, String> params) {
        
        // Extract filter parameters (those starting with 'filter_')
        Map<String, String> filterParams = new HashMap<>();
        
        if (params != null) {
            params.forEach((key, value) -> {
                if (key.startsWith("filter_")) {
                    // Remove the 'filter_' prefix
                    String filterKey = key.substring(7);
                    filterParams.put(filterKey, value);
                }
            });
        }
        
        // Build specification from filter parameters
        SpecificationBuilder<T> builder = new SpecificationBuilder<>();
        return builder.buildSpecification(filterParams);
    }
}