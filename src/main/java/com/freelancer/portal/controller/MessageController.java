package com.freelancer.portal.controller;

import com.freelancer.portal.dto.MessageDto;
import com.freelancer.portal.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // Project-based messaging endpoints
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<Page<MessageDto>> getMessagesByProject(
            @PathVariable Long projectId,
            Pageable pageable) {
        return ResponseEntity.ok(messageService.getMessagesByProject(projectId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<MessageDto> getMessageById(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.getMessageById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<MessageDto> createMessage(@RequestBody MessageDto messageDto) {
        return new ResponseEntity<>(messageService.createMessage(messageDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<Void> markMessageAsRead(@PathVariable Long id) {
        messageService.markMessageAsRead(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/project/{projectId}/unread-count")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<Long> countUnreadMessages(@PathVariable Long projectId) {
        return ResponseEntity.ok(messageService.countUnreadMessages(projectId));
    }

    // Conversation-based messaging endpoints
    @PostMapping(value = "/conversation/{conversationId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<MessageDto> sendMessage(
            @PathVariable Long conversationId,
            @RequestParam("content") String content,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        return new ResponseEntity<>(messageService.sendMessage(conversationId, content, file), HttpStatus.CREATED);
    }
    
    // Added JSON endpoint for sending messages
    @PostMapping(value = "/conversation/{conversationId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<MessageDto> sendJsonMessage(
            @PathVariable Long conversationId,
            @RequestBody Map<String, String> payload) throws IOException {
        String content = payload.get("content");
        return new ResponseEntity<>(messageService.sendMessage(conversationId, content, null), HttpStatus.CREATED);
    }

    @PostMapping(value = "/conversation/{conversationId}/attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<MessageDto> sendMessageWithAttachment(
            @PathVariable Long conversationId,
            @RequestParam("content") String content,
            @RequestParam("file") MultipartFile file) throws IOException {
        return new ResponseEntity<>(messageService.sendMessage(conversationId, content, file), HttpStatus.CREATED);
    }

    @GetMapping("/conversation/{conversationId}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<Page<MessageDto>> getMessagesByConversation(
            @PathVariable Long conversationId,
            Pageable pageable) {
        return ResponseEntity.ok(messageService.getMessagesByConversation(conversationId, pageable));
    }

    @GetMapping("/conversation/{conversationId}/unread-count")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<Long> countUnreadConversationMessages(@PathVariable Long conversationId) {
        return ResponseEntity.ok(messageService.countUnreadConversationMessages(conversationId));
    }
}