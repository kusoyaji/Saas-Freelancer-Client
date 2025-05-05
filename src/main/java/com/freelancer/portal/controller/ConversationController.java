package com.freelancer.portal.controller;

import com.freelancer.portal.dto.ConversationDto;
import com.freelancer.portal.dto.ParticipantRequestDto;
import com.freelancer.portal.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<ConversationDto> createConversation(@Valid @RequestBody ParticipantRequestDto request) {
        return new ResponseEntity<>(conversationService.createConversation(request.getParticipantIds()), HttpStatus.CREATED);
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<ConversationDto> createConversationSafely(@Valid @RequestBody ParticipantRequestDto request) {
        return new ResponseEntity<>(conversationService.createConversation(request.getParticipantIds()), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<Page<ConversationDto>> getConversationsForCurrentUser(Pageable pageable) {
        // The service will extract the current user ID from the security context
        return ResponseEntity.ok(conversationService.getConversationsForUser(null, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<ConversationDto> getConversationById(@PathVariable Long id) {
        return ResponseEntity.ok(conversationService.getConversationById(id));
    }

    @PostMapping("/{id}/participants/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<ConversationDto> addParticipant(
            @PathVariable Long id, 
            @PathVariable Long userId) {
        return ResponseEntity.ok(conversationService.addParticipant(id, userId));
    }

    @DeleteMapping("/{id}/participants/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<ConversationDto> removeParticipant(
            @PathVariable Long id, 
            @PathVariable Long userId) {
        return ResponseEntity.ok(conversationService.removeParticipant(id, userId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long id) {
        conversationService.deleteConversation(id);
        return ResponseEntity.noContent().build();
    }
}