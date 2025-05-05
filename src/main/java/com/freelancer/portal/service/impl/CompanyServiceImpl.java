package com.freelancer.portal.service.impl;

import com.freelancer.portal.dto.CompanyDto;
import com.freelancer.portal.model.Company;
import com.freelancer.portal.model.User;
import com.freelancer.portal.repository.CompanyRepository;
import com.freelancer.portal.repository.UserRepository;
import com.freelancer.portal.exception.ResourceNotFoundException;
import com.freelancer.portal.security.SecurityUtils;
import com.freelancer.portal.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the CompanyService interface.
 */
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public CompanyDto getCurrentUserCompany() {
        User currentUser = securityUtils.getCurrentUser();
        Company company = companyRepository.findByOwnerId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found for current user"));
        return mapToDto(company);
    }

    @Override
    @Transactional
    public CompanyDto createOrUpdateCompany(CompanyDto companyDto) {
        User currentUser = securityUtils.getCurrentUser();
        Company company = companyRepository.findByOwnerId(currentUser.getId())
                .orElse(new Company());
        
        updateCompanyFromDto(company, companyDto);
        
        if (company.getOwner() == null) {
            company.setOwner(currentUser);
        }
        
        company = companyRepository.save(company);
        return mapToDto(company);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyDto getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
        return mapToDto(company);
    }

    @Override
    @Transactional
    public CompanyDto createCompany(CompanyDto companyDto) {
        User currentUser = securityUtils.getCurrentUser();
        Company company = new Company();
        updateCompanyFromDto(company, companyDto);
        company.setOwner(currentUser);
        company = companyRepository.save(company);
        return mapToDto(company);
    }

    @Override
    @Transactional
    public CompanyDto updateCompany(Long id, CompanyDto companyDto) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
        updateCompanyFromDto(company, companyDto);
        company = companyRepository.save(company);
        return mapToDto(company);
    }

    @Override
    @Transactional
    public void deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
        companyRepository.delete(company);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOwner(Long id) {
        User currentUser = securityUtils.getCurrentUser();
        return companyRepository.existsByIdAndOwnerId(id, currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCompanyOwner(Long id) {
        User currentUser = securityUtils.getCurrentUser();
        return companyRepository.existsByIdAndOwnerId(id, currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompanyDto> getAllCompanies(Pageable pageable) {
        return companyRepository.findAll(pageable).map(this::mapToDto);
    }

    /**
     * Maps a Company entity to a CompanyDto.
     *
     * @param company the company entity
     * @return the company DTO
     */
    private CompanyDto mapToDto(Company company) {
        if (company == null) {
            return null;
        }
        
        return CompanyDto.builder()
                .id(company.getId())
                .name(company.getName())
                .address(company.getAddress())
                .city(company.getCity())
                .state(company.getState())
                .zipCode(company.getPostalCode())
                .country(company.getCountry())
                .taxId(company.getTaxId())
                .phone(company.getPhone())
                .email(company.getEmail())
                .website(company.getWebsite())
                .logo(company.getLogoUrl())
                .ownerId(company.getOwner() != null ? company.getOwner().getId() : null)
                .ownerName(company.getOwner() != null ? 
                        company.getOwner().getFirstName() + " " + company.getOwner().getLastName() : null)
                .build();
    }

    /**
     * Updates a Company entity from a CompanyDto.
     *
     * @param company the company entity to update
     * @param dto the company DTO with new values
     */
    private void updateCompanyFromDto(Company company, CompanyDto dto) {
        company.setName(dto.getName());
        company.setAddress(dto.getAddress());
        company.setCity(dto.getCity());
        company.setState(dto.getState());
        company.setPostalCode(dto.getZipCode());
        company.setCountry(dto.getCountry());
        company.setTaxId(dto.getTaxId());
        company.setPhone(dto.getPhone());
        company.setEmail(dto.getEmail());
        company.setWebsite(dto.getWebsite());
        company.setLogoUrl(dto.getLogo());
    }
}