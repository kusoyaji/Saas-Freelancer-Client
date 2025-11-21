package com.freelancer.portal.service.impl;

import com.freelancer.portal.dto.UserDTO;
import com.freelancer.portal.dto.UserUpdateDto;
import com.freelancer.portal.exception.FileStorageException;
import com.freelancer.portal.mapper.UserMapper;
import com.freelancer.portal.model.User;
import com.freelancer.portal.repository.UserRepository;
import com.freelancer.portal.security.SecurityUtils;
import com.freelancer.portal.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.profile-pictures-dir:uploads/profile-pictures}")
    private String profilePicturesDir;

    @Override
    public UserDTO getCurrentUser() {
        User user = securityUtils.getCurrentUser();
        return UserDTO.fromEntity(user);
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return UserDTO.fromEntity(user);
    }

    @Override
    public UserDTO updateCurrentUser(UserUpdateDto userUpdateDto) {
        User currentUser = securityUtils.getCurrentUser();
        return updateUser(currentUser.getId(), userUpdateDto);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserUpdateDto userUpdateDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        // Handle email specially (check for duplicates and reset verification)
        if (userUpdateDto.getEmail() != null && !userUpdateDto.getEmail().equals(user.getEmail())) {
            // Check if email already exists
            if (userRepository.existsByEmail(userUpdateDto.getEmail())) {
                throw new IllegalStateException("Email already in use: " + userUpdateDto.getEmail());
            }
            user.setEmail(userUpdateDto.getEmail());
            // Reset email verification when email is changed
            user.setEmailVerified(false);
        }

        // Use the mapper to update all other user fields from DTO
        user = UserMapper.updateFromDto(user, userUpdateDto);

        // Update additional fields that might not be in the mapper
        if (userUpdateDto.getPhone() != null) {
            user.setPhone(userUpdateDto.getPhone());
        }
        if (userUpdateDto.getBio() != null) {
            user.setBio(userUpdateDto.getBio());
        }
        if (userUpdateDto.getWebsite() != null) {
            user.setWebsite(userUpdateDto.getWebsite());
        }
        if (userUpdateDto.getCompany() != null) {
            user.setCompany(userUpdateDto.getCompany());
        }
        if (userUpdateDto.getPosition() != null) {
            user.setPosition(userUpdateDto.getPosition());
        }
        if (userUpdateDto.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(userUpdateDto.getProfilePictureUrl());
        }

        // Password changes should be handled by a separate service method with proper security

        User savedUser = userRepository.save(user);
        return UserDTO.fromEntity(savedUser);
    }

    @Override
    public void deleteUser(Long id) {
        log.debug("Deleting user with id: {}", id);
        userRepository.deleteById(id);
    }

    @Override
    public User saveUser(User user) {
        log.debug("Saving user: {}", user.getEmail());
        return userRepository.save(user);
    }

    @Override
    public User updateUser(User user) {
        log.debug("Updating user: {}", user.getEmail());
        return userRepository.save(user);
    }

    @Override
    public Optional<User> getUserEntityById(Long id) {
        log.debug("Getting user entity by id: {}", id);
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        log.debug("Getting user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        log.debug("Getting all users with pagination");
        return userRepository.findAll(pageable);
    }

    @Override
    public boolean existsByEmail(String email) {
        log.debug("Checking if user exists with email: {}", email);
        return userRepository.existsByEmail(email);
    }

    @Override
    public UserDTO getUserDTOByEmail(String email) {
        log.debug("Getting user DTO by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return UserDTO.fromEntity(user);
    }

    @Override
    public UserDTO getUserDTOById(Long id) {
        log.debug("Getting user DTO by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return UserDTO.fromEntity(user);
    }

    @Override
    @Transactional
    public UserDTO updateUserDTO(Long id, UserDTO userDTO) {
        log.debug("Updating user DTO with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        
        // Update only allowed fields
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setPhone(userDTO.getPhone());
        user.setBio(userDTO.getBio());
        user.setWebsite(userDTO.getWebsite());
        user.setCompany(userDTO.getCompany());
        user.setPosition(userDTO.getPosition());
        user.setProfilePictureUrl(userDTO.getProfilePictureUrl());
        
        // Email and role changes are typically handled separately with additional security
        
        User savedUser = userRepository.save(user);
        return UserDTO.fromEntity(savedUser);
    }

    @Override
    @Transactional
    public UserDTO updateProfilePicture(MultipartFile file) throws IOException {
        log.info("Attempting to update profile picture");

        if (file.isEmpty()) {
            log.error("Profile picture upload failed: Cannot store empty file.");
            throw new FileStorageException("Cannot store empty file");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.error("Profile picture upload failed: Invalid file type: {}", contentType);
            throw new FileStorageException("Only image files are allowed for profile pictures");
        }

        // Validate file size (e.g., max 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            log.error("Profile picture upload failed: File size {} exceeds maximum allowed size {}", 
                      file.getSize(), maxSize);
            throw new FileStorageException("File size exceeds maximum allowed size of 5MB");
        }

        User currentUser = securityUtils.getCurrentUser();
        log.debug("Current user ID: {}", currentUser.getId());

        // Delete old profile picture if exists
        if (currentUser.getProfilePictureUrl() != null && !currentUser.getProfilePictureUrl().isEmpty()) {
            deleteOldProfilePicture(currentUser.getProfilePictureUrl());
        }

        // Generate unique filename
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = originalFilename.substring(dotIndex);
        }
        String storedFilename = "profile_" + currentUser.getId() + "_" + UUID.randomUUID() + fileExtension;
        log.debug("Storing profile picture as: {}", storedFilename);

        // Create profile pictures directory if it doesn't exist
        Path uploadPath = Paths.get(profilePicturesDir).toAbsolutePath().normalize();
        log.debug("Profile pictures directory: {}", uploadPath);

        try {
            if (!Files.exists(uploadPath)) {
                log.info("Creating profile pictures directory: {}", uploadPath);
                Files.createDirectories(uploadPath);
            }

            // Copy file to the target location
            Path targetLocation = uploadPath.resolve(storedFilename);
            log.debug("Copying file to: {}", targetLocation);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Profile picture successfully stored at {}", targetLocation);

            // Update user's profile picture URL
            // Store relative path that can be served by the application
            String profilePictureUrl = "/uploads/profile-pictures/" + storedFilename;
            currentUser.setProfilePictureUrl(profilePictureUrl);
            
            User savedUser = userRepository.save(currentUser);
            log.info("User profile picture URL updated successfully");
            
            return UserDTO.fromEntity(savedUser);

        } catch (IOException ex) {
            log.error("Failed to store profile picture {}: {}", originalFilename, ex.getMessage(), ex);
            throw new FileStorageException("Failed to store profile picture " + originalFilename, ex);
        }
    }

    @Override
    @Transactional
    public UserDTO deleteProfilePicture() throws IOException {
        log.info("Attempting to delete profile picture");

        User currentUser = securityUtils.getCurrentUser();
        log.debug("Current user ID: {}", currentUser.getId());

        if (currentUser.getProfilePictureUrl() != null && !currentUser.getProfilePictureUrl().isEmpty()) {
            deleteOldProfilePicture(currentUser.getProfilePictureUrl());
            currentUser.setProfilePictureUrl(null);
            
            User savedUser = userRepository.save(currentUser);
            log.info("User profile picture deleted successfully");
            
            return UserDTO.fromEntity(savedUser);
        } else {
            log.info("No profile picture to delete");
            return UserDTO.fromEntity(currentUser);
        }
    }

    /**
     * Helper method to delete old profile picture file.
     */
    private void deleteOldProfilePicture(String profilePictureUrl) {
        try {
            // Extract filename from URL (e.g., /uploads/profile-pictures/filename.jpg -> filename.jpg)
            String filename = profilePictureUrl.substring(profilePictureUrl.lastIndexOf('/') + 1);
            Path filePath = Paths.get(profilePicturesDir).resolve(filename).toAbsolutePath().normalize();
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Deleted old profile picture: {}", filePath);
            } else {
                log.warn("Old profile picture not found: {}", filePath);
            }
        } catch (IOException ex) {
            log.error("Failed to delete old profile picture: {}", ex.getMessage(), ex);
            // Don't throw exception - continue with the upload even if old file deletion fails
        }
    }
}