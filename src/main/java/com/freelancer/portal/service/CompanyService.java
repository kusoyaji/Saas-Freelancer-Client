package com.freelancer.portal.service;

import com.freelancer.portal.dto.CompanyDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for company operations.
 */
public interface CompanyService {

    /**
     * Get the current user's company details.
     *
     * @return the company DTO
     */
    CompanyDto getCurrentUserCompany();

    /**
     * Create or update the current user's company details.
     *
     * @param companyDto the company information
     * @return the updated company DTO
     */
    CompanyDto createOrUpdateCompany(CompanyDto companyDto);

    /**
     * Get a company by ID.
     *
     * @param id the company ID
     * @return the company DTO
     */
    CompanyDto getCompanyById(Long id);

    /**
     * Create a new company.
     *
     * @param companyDto the company data
     * @return the created company
     */
    CompanyDto createCompany(CompanyDto companyDto);

    /**
     * Update a company.
     *
     * @param id the company ID
     * @param companyDto the company data
     * @return the updated company
     */
    CompanyDto updateCompany(Long id, CompanyDto companyDto);

    /**
     * Delete a company by ID.
     *
     * @param id the company ID
     */
    void deleteCompany(Long id);

    /**
     * Check if the current user is the owner of the company.
     *
     * @param id the company ID
     * @return true if the current user is the owner, false otherwise
     */
    boolean isOwner(Long id);

    /**
     * Check if a company belongs to the current user.
     *
     * @param id the company ID
     * @return true if the company belongs to the current user, false otherwise
     */
    boolean isCompanyOwner(Long id);

    /**
     * Get all companies with pagination.
     *
     * @param pageable pagination information
     * @return a page of companies
     */
    Page<CompanyDto> getAllCompanies(Pageable pageable);
}