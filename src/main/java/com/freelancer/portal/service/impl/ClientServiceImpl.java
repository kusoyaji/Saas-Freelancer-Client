package com.freelancer.portal.service.impl;

import com.freelancer.portal.dto.ClientDto;
import com.freelancer.portal.dto.ProjectDto;
import com.freelancer.portal.exception.ResourceNotFoundException;
import com.freelancer.portal.mapper.ClientMapper;
import com.freelancer.portal.model.Client;
import com.freelancer.portal.model.Company;
import com.freelancer.portal.model.Project;
import com.freelancer.portal.model.User;
import com.freelancer.portal.repository.ClientRepository;
import com.freelancer.portal.repository.CompanyRepository;
import com.freelancer.portal.repository.ProjectRepository;
import com.freelancer.portal.repository.UserRepository;
import com.freelancer.portal.security.SecurityUtils;
import com.freelancer.portal.service.ClientService;
import com.freelancer.portal.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final CompanyRepository companyRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final ProjectService projectService;

    @Override
    @Transactional(readOnly = true)
    public Page<ClientDto> getAllClients(Pageable pageable) {
        User currentUser = securityUtils.getCurrentUser();
        Page<Client> clients = clientRepository.findAllByFreelancer(currentUser, pageable);
        
        // Map clients to DTOs with project counts
        return clients.map(client -> {
            List<Project> projects = projectRepository.findByClient(client);
            return ClientMapper.toDtoWithProjects(client, projects);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public ClientDto getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
        
        if (!isClientOwner(id)) {
            throw new ResourceNotFoundException("Client not found with id: " + id);
        }
        
        List<Project> projects = projectRepository.findByClient(client);
        return ClientMapper.toDtoWithProjects(client, projects);
    }

    @Override
    @Transactional
    public ClientDto createClient(ClientDto clientDto) {
        User currentUser = securityUtils.getCurrentUser();
        Client client = new Client();
        
        // Use the mapper to update the client entity from the DTO
        client = ClientMapper.updateFromDto(client, clientDto);
        
        // Set company if provided
        if (clientDto.getCompanyId() != null) {
            Company company = companyRepository.findById(clientDto.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + clientDto.getCompanyId()));
            client.setCompany(company);
        }
        
        // Set user if provided
        if (clientDto.getUserId() != null) {
            User user = userRepository.findById(clientDto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + clientDto.getUserId()));
            client.setUser(user);
        }
        
        // Set freelancer
        client.setFreelancer(currentUser);
        
        Client savedClient = clientRepository.save(client);
        
        // Return client with project count (which will be 0 for new clients)
        List<Project> projects = projectRepository.findByClient(savedClient);
        return ClientMapper.toDtoWithProjects(savedClient, projects);
    }

    @Override
    @Transactional
    public ClientDto updateClient(Long id, ClientDto clientDto) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
        
        if (!isClientOwner(id)) {
            throw new ResourceNotFoundException("Client not found with id: " + id);
        }
        
        // Use the mapper to update the client entity
        client = ClientMapper.updateFromDto(client, clientDto);

        // Update company if provided
        if (clientDto.getCompanyId() != null) {
            Company company = companyRepository.findById(clientDto.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + clientDto.getCompanyId()));
            client.setCompany(company);
        } else {
            client.setCompany(null);
        }
        
        // Update user if provided
        if (clientDto.getUserId() != null) {
            User user = userRepository.findById(clientDto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + clientDto.getUserId()));
            client.setUser(user);
        } else {
            client.setUser(null);
        }
        
        Client updatedClient = clientRepository.save(client);
        List<Project> projects = projectRepository.findByClient(updatedClient);
        return ClientMapper.toDtoWithProjects(updatedClient, projects);
    }

    @Override
    @Transactional
    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
        
        if (!isClientOwner(id)) {
            throw new ResourceNotFoundException("Client not found with id: " + id);
        }
        
        // Check if client has any active projects
        long projectCount = projectRepository.countByClient(client);
        if (projectCount > 0) {
            throw new IllegalStateException("Cannot delete client with active projects");
        }
        
        clientRepository.delete(client);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isClientOwner(Long id) {
        User currentUser = securityUtils.getCurrentUser();
        Client client = clientRepository.findById(id).orElse(null);
        
        if (client == null) {
            return false;
        }
        
        return client.getFreelancer().getId().equals(currentUser.getId());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDto> getClientProjects(Long clientId, Pageable pageable) {
        if (!isClientOwner(clientId)) {
            throw new ResourceNotFoundException("Client not found with id: " + clientId);
        }
        
        return projectService.getProjectsByClient(clientId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getClientProjectsCount(Long clientId) {
        if (!isClientOwner(clientId)) {
            throw new ResourceNotFoundException("Client not found with id: " + clientId);
        }
        
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));
        
        return projectRepository.countByClient(client);
    }
}