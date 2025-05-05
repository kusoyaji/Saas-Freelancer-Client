package com.freelancer.portal.repository;

import com.freelancer.portal.model.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Company entity operations.
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /**
     * Find a company by owner ID.
     *
     * @param ownerId the ID of the company owner
     * @return an optional containing the company if found, empty otherwise
     */
    Optional<Company> findByOwnerId(Long ownerId);

    /**
     * Check if a company exists with the given ID and owner ID.
     *
     * @param id the company ID
     * @param ownerId the owner ID
     * @return true if a company exists with the given ID and owner ID, false otherwise
     */
    boolean existsByIdAndOwnerId(Long id, Long ownerId);

    /**
     * Find companies by name containing the given string.
     *
     * @param name the name pattern to search for
     * @param pageable pagination information
     * @return a page of companies matching the search criteria
     */
    Page<Company> findByNameContainingIgnoreCase(String name, Pageable pageable);
}