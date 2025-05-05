package com.freelancer.portal.controller;

import com.freelancer.portal.dto.ClientDto;

import com.freelancer.portal.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for client management operations.
 */
@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    /**
     * Get all clients for the current user with pagination.
     */
    @GetMapping
    public ResponseEntity<Page<ClientDto>> getAllClients(Pageable pageable) {
        return ResponseEntity.ok(clientService.getAllClients(pageable));
    }

    /**
     * Get a client by ID.
     * Only accessible if the client belongs to the current user.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<ClientDto> getClientById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    /**
     * Create a new client.
     */
    @PostMapping
    public ResponseEntity<ClientDto> createClient(@Valid @RequestBody ClientDto clientDto) {
        return new ResponseEntity<>(clientService.createClient(clientDto), HttpStatus.CREATED);
    }

    /**
     * Update an existing client.
     * Only accessible if the client belongs to the current user.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<ClientDto> updateClient(
            @PathVariable Long id, @Valid @RequestBody ClientDto clientDto) {
        return ResponseEntity.ok(clientService.updateClient(id, clientDto));
    }

    /**
     * Delete a client.
     * Only accessible if the client belongs to the current user.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }
}