package com.freelancer.portal.service;

import com.freelancer.portal.dto.ClientDto;
import com.freelancer.portal.dto.ProjectDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientService {
    Page<ClientDto> getAllClients(Pageable pageable);
    ClientDto getClientById(Long id);
    ClientDto createClient(ClientDto clientDto);
    ClientDto updateClient(Long id, ClientDto clientDto);
    void deleteClient(Long id);
    boolean isClientOwner(Long id);
    Page<ProjectDto> getClientProjects(Long clientId, Pageable pageable);
    Long getClientProjectsCount(Long clientId);
}