package com.freelancer.portal.mapper;

import com.freelancer.portal.dto.ClientDto;
import com.freelancer.portal.model.Client;
import com.freelancer.portal.model.Project;

import java.util.List;

/**
 * Utility class for mapping Client entities to DTOs and vice versa.
 */
public class ClientMapper {

    /**
     * Maps a Client entity to a ClientDto.
     *
     * @param client the client entity
     * @return the client DTO
     */
    public static ClientDto toDto(Client client) {
        if (client == null) {
            return null;
        }
        
        ClientDto.ClientDtoBuilder builder = ClientDto.builder()
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
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt());
        
        // Set freelancer information if available
        if (client.getFreelancer() != null) {
            builder.freelancerId(client.getFreelancer().getId());
        }
        
        ClientDto dto = builder.build();
        
        // Set company information if available
        if (client.getCompany() != null) {
            dto.setCompanyId(client.getCompany().getId());
            dto.setCompanyName(client.getCompany().getName());
        }
        
        // Set user information if available
        if (client.getUser() != null) {
            dto.setUserId(client.getUser().getId());
        }
        
        return dto;
    }

    /**
     * Maps a Client entity to a ClientDto with project count information.
     *
     * @param client the client entity
     * @param projects the list of projects associated with this client (can be null)
     * @return the enhanced client DTO
     */
    public static ClientDto toDtoWithProjects(Client client, List<Project> projects) {
        ClientDto dto = toDto(client);
        
        if (dto != null) {
            // Set project count to 0 if projects is null, otherwise set to actual size
            dto.setProjectsCount(projects == null ? 0L : (long) projects.size());
        }
        
        return dto;
    }
    
    /**
     * Updates a Client entity from a ClientDto.
     *
     * @param client the client entity to update
     * @param dto the DTO with new values
     * @return the updated client entity
     */
    public static Client updateFromDto(Client client, ClientDto dto) {
        if (dto == null) {
            return client;
        }
        
        if (dto.getName() != null) {
            client.setName(dto.getName());
        }
        if (dto.getEmail() != null) {
            client.setEmail(dto.getEmail());
        }
        client.setPhone(dto.getPhone());
        client.setAddress(dto.getAddress());
        client.setCity(dto.getCity());
        client.setState(dto.getState());
        client.setPostalCode(dto.getZipCode());
        client.setCountry(dto.getCountry());
        client.setCurrency(dto.getCurrency());
        client.setNotes(dto.getNotes());
        client.setWebsite(dto.getWebsite());
        
        // Company and User would typically be updated through their own services
        
        return client;
    }
}