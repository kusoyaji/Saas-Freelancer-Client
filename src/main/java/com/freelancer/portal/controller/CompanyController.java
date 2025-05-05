package com.freelancer.portal.controller;

import com.freelancer.portal.dto.CompanyDto;
import com.freelancer.portal.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for company operations.
 */
@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    /**
     * Get the current user's company details.
     */
    @GetMapping("/mine")
    public ResponseEntity<CompanyDto> getCurrentUserCompany() {
        return ResponseEntity.ok(companyService.getCurrentUserCompany());
    }

    /**
     * Create or update the current user's company details.
     */
    @PostMapping
    public ResponseEntity<CompanyDto> createOrUpdateCompany(@Valid @RequestBody CompanyDto companyDto) {
        return ResponseEntity.ok(companyService.createOrUpdateCompany(companyDto));
    }

    /**
     * Get a company by ID.
     * Only accessible to ADMIN users.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanyDto> getCompanyById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    /**
     * Delete a company by ID.
     * Only accessible to ADMIN users or the company owner.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @companyService.isOwner(#id)")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }
}