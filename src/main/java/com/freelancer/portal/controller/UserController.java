package com.freelancer.portal.controller;

import com.freelancer.portal.dto.UserDTO;
import com.freelancer.portal.dto.UserUpdateDto;
import com.freelancer.portal.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for user management operations.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Get all users with pagination.
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers(Pageable pageable) {
        // Convert page of users to a list of UserDTOs
        List<UserDTO> users = userService.getAllUsers(pageable)
            .stream()
            .map(UserDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    /**
     * Get the current authenticated user's details.
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    /**
     * Get a user by their ID.
     * Only accessible to users with the ADMIN role.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * Update the current user's profile.
     */
    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateCurrentUser(@Valid @RequestBody UserUpdateDto userUpdateDto) {
        return ResponseEntity.ok(userService.updateCurrentUser(userUpdateDto));
    }

    /**
     * Update a user by their ID.
     * Only accessible to users with the ADMIN role.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDto userUpdateDto) {
        return ResponseEntity.ok(userService.updateUser(id, userUpdateDto));
    }

    /**
     * Delete a user by their ID.
     * Only accessible to users with the ADMIN role.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FREELANCER', 'ROLE_CLIENT')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Upload profile picture for the current user.
     */
    @PostMapping("/me/profile-picture")
    public ResponseEntity<UserDTO> uploadProfilePicture(@RequestParam("file") MultipartFile file) throws IOException {
        UserDTO updatedUser = userService.updateProfilePicture(file);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Delete profile picture for the current user.
     */
    @DeleteMapping("/me/profile-picture")
    public ResponseEntity<UserDTO> deleteProfilePicture() throws IOException {
        UserDTO updatedUser = userService.deleteProfilePicture();
        return ResponseEntity.ok(updatedUser);
    }
}