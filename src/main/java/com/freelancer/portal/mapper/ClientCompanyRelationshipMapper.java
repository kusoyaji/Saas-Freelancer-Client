package com.freelancer.portal.mapper;

import com.freelancer.portal.dto.ClientDto;
import com.freelancer.portal.dto.CompanyDto;
import com.freelancer.portal.model.Client;
import com.freelancer.portal.model.Company;
import com.freelancer.portal.model.Project;
import com.freelancer.portal.model.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for mapping relationships between Client and Company entities.
 */
public class ClientCompanyRelationshipMapper {
    
    /**
     * Map Client entity to ClientDto, including company information.
     * 
     * @param client the client entity
     * @param projectsCount count of projects associated with this client
     * @return the client DTO with company information
     */
    public static ClientDto toDto(Client client, Long projectsCount) {
        if (client == null) {
            return null;
        }
        
        return ClientDto.builder()
                .id(client.getId())
                .name(client.getName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .address(client.getAddress())
                .city(client.getCity())
                .state(client.getState())
                .zipCode(client.getPostalCode())
                .country(client.getCountry())
                .currency(client.getCurrency())
                .notes(client.getNotes())
                .website(client.getWebsite())
                .companyId(client.getCompany() != null ? client.getCompany().getId() : null)
                .companyName(client.getCompany() != null ? client.getCompany().getName() : null)
                .freelancerId(client.getFreelancer() != null ? client.getFreelancer().getId() : null)
                .userId(client.getFreelancer() != null ? client.getFreelancer().getId() : null)
                .projectsCount(projectsCount)
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .build();
    }
    
    /**
     * Map Company entity to CompanyDto, including owner information.
     * 
     * @param company the company entity
     * @return the company DTO with owner information
     */
    public static CompanyDto toDto(Company company) {
        if (company == null) {
            return null;
        }
        
        User owner = company.getOwner();
        String ownerName = owner != null ? 
                owner.getFirstName() + " " + owner.getLastName() : null;
                
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
                .ownerId(owner != null ? owner.getId() : null)
                .ownerName(ownerName)
                .build();
    }

    /**
     * Get list of projects associated with a client.
     * This method handles the relationship between clients and projects,
     * ensuring proper access to associated project data.
     *
     * @param client the client entity
     * @return list of associated projects with proper relationship management
     */
    public static List<Project> getClientProjects(Client client) {
        if (client == null) {
            return List.of();
        }

        // Get projects from client and ensure relationships are initialized
        List<Project> projects = client.getProjects();
        
        // If using lazy loading, may need to ensure projects are actually loaded
        if (projects != null && !projects.isEmpty()) {
            // Ensure each project has its client reference set correctly
            projects.forEach(project -> {
                if (project.getClient() == null) {
                    project.setClient(client);
                }
            });
        }

        return projects != null ? projects : List.of();
    }

    /**
     * Get list of clients associated with a company.
     *
     * @param company the company entity
     * @return list of associated clients
     */
    public static List<Client> getCompanyClients(Company company) {
        if (company == null) {
            return List.of();
        }

        return company.getClients();
    }

    /**
     * Update Client entity with DTO values.
     *
     * @param client the client entity to update
     * @param clientDto the client DTO with new values
     * @param company the company to associate (can be null)
     * @param freelancer the freelancer to associate
     * @return the updated client entity
     */
    public static Client updateClientFromDto(Client client, ClientDto clientDto, Company company, User freelancer) {
        if (client != null && clientDto != null) {
            client.setName(clientDto.getName());
            client.setEmail(clientDto.getEmail());

            if (clientDto.getPhone() != null) {
                client.setPhone(clientDto.getPhone());
            }

            if (clientDto.getAddress() != null) {
                client.setAddress(clientDto.getAddress());
            }

            if (clientDto.getCity() != null) {
                client.setCity(clientDto.getCity());
            }

            if (clientDto.getState() != null) {
                client.setState(clientDto.getState());
            }

            if (clientDto.getZipCode() != null) {
                client.setPostalCode(clientDto.getZipCode());
            }

            if (clientDto.getCountry() != null) {
                client.setCountry(clientDto.getCountry());
            }

            if (clientDto.getCurrency() != null) {
                client.setCurrency(clientDto.getCurrency());
            }

            if (clientDto.getNotes() != null) {
                client.setNotes(clientDto.getNotes());
            }

            if (clientDto.getWebsite() != null) {
                client.setWebsite(clientDto.getWebsite());
            }

            // Set company if provided
            client.setCompany(company);

            // Always ensure freelancer is set
            if (freelancer != null) {
                client.setFreelancer(freelancer);
            }
        }

        return client;
    }

    /**
     * Create a new Client entity from DTO.
     *
     * @param clientDto the client DTO
     * @param company the company to associate (can be null)
     * @param freelancer the freelancer to associate
     * @return a new client entity
     */
    public static Client createClientFromDto(ClientDto clientDto, Company company, User freelancer) {
        return Client.builder()
                .name(clientDto.getName())
                .email(clientDto.getEmail())
                .phone(clientDto.getPhone())
                .address(clientDto.getAddress())
                .city(clientDto.getCity())
                .state(clientDto.getState())
                .postalCode(clientDto.getZipCode())
                .country(clientDto.getCountry())
                .currency(clientDto.getCurrency())
                .notes(clientDto.getNotes())
                .website(clientDto.getWebsite())
                .company(company)
                .freelancer(freelancer)
                .build();
    }

    /**
     * Update Company entity with DTO values.
     *
     * @param company the company entity to update
     * @param companyDto the company DTO with new values
     * @param owner the owner to associate
     * @return the updated company entity
     */
    public static Company updateCompanyFromDto(Company company, CompanyDto companyDto, User owner) {
        if (company != null && companyDto != null) {
            company.setName(companyDto.getName());

            if (companyDto.getAddress() != null) {
                company.setAddress(companyDto.getAddress());
            }

            if (companyDto.getCity() != null) {
                company.setCity(companyDto.getCity());
            }

            if (companyDto.getState() != null) {
                company.setState(companyDto.getState());
            }

            if (companyDto.getZipCode() != null) {
                company.setPostalCode(companyDto.getZipCode());
            }

            if (companyDto.getCountry() != null) {
                company.setCountry(companyDto.getCountry());
            }

            if (companyDto.getTaxId() != null) {
                company.setTaxId(companyDto.getTaxId());
            }

            if (companyDto.getPhone() != null) {
                company.setPhone(companyDto.getPhone());
            }

            if (companyDto.getEmail() != null) {
                company.setEmail(companyDto.getEmail());
            }

            if (companyDto.getWebsite() != null) {
                company.setWebsite(companyDto.getWebsite());
            }

            if (companyDto.getLogo() != null) {
                company.setLogoUrl(companyDto.getLogo());
            }

            // Set owner if provided
            if (owner != null) {
                company.setOwner(owner);
            }
        }

        return company;
    }

    /**
     * Create a new Company entity from DTO.
     *
     * @param companyDto the company DTO
     * @param owner the owner to associate
     * @return a new company entity
     */
    public static Company createCompanyFromDto(CompanyDto companyDto, User owner) {
        return Company.builder()
                .name(companyDto.getName())
                .address(companyDto.getAddress())
                .city(companyDto.getCity())
                .state(companyDto.getState())
                .postalCode(companyDto.getZipCode())
                .country(companyDto.getCountry())
                .taxId(companyDto.getTaxId())
                .phone(companyDto.getPhone())
                .email(companyDto.getEmail())
                .website(companyDto.getWebsite())
                .logoUrl(companyDto.getLogo())
                .owner(owner)
                .build();
    }
}