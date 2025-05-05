package com.freelancer.portal.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Extended error response for validation errors.
 * Includes field-specific validation error messages.
 */
@Getter
@Setter
@NoArgsConstructor
public class ValidationErrorResponse extends ErrorResponse {
    
    private Map<String, String> errors;
    
    public ValidationErrorResponse(int status, String error, String message, String path, Map<String, String> errors) {
        super(status, error, message, path);
        this.errors = errors;
    }
}