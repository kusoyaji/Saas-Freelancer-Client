package com.freelancer.portal.service;

import com.freelancer.portal.dto.UserDTO;
import com.freelancer.portal.dto.UserUpdateDto;
import com.freelancer.portal.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

/**
 * Service interface for user operations.
 */
public interface UserService {

    /**
     * Get the current authenticated user's details.
     *
     * @return the user DTO
     */
    UserDTO getCurrentUser();

    /**
     * Get a user by their ID.
     *
     * @param id the user ID
     * @return the user DTO
     */
    UserDTO getUserById(Long id);

    /**
     * Update the current user's profile.
     *
     * @param userUpdateDto the user update DTO
     * @return the updated user DTO
     */
    UserDTO updateCurrentUser(UserUpdateDto userUpdateDto);

    /**
     * Update a user by their ID.
     *
     * @param id the user ID
     * @param userUpdateDto the user update DTO
     * @return the updated user DTO
     */
    UserDTO updateUser(Long id, UserUpdateDto userUpdateDto);

    /**
     * Delete a user by their ID.
     *
     * @param id the user ID
     */
    void deleteUser(Long id);

    // Entity-based methods
    User saveUser(User user);
    User updateUser(User user);
    Optional<User> getUserEntityById(Long id);
    Optional<User> getUserByEmail(String email);
    Page<User> getAllUsers(Pageable pageable);
    boolean existsByEmail(String email);
    
    // DTO-based methods for controller use
    UserDTO getUserDTOByEmail(String email);
    UserDTO getUserDTOById(Long id);
    UserDTO updateUserDTO(Long id, UserDTO userDTO);

    /**
     * Update the current user's profile picture.
     *
     * @param file the profile picture file
     * @return the updated user DTO
     * @throws IOException if an I/O error occurs
     */
    UserDTO updateProfilePicture(MultipartFile file) throws IOException;

    /**
     * Delete the current user's profile picture.
     *
     * @return the updated user DTO
     * @throws IOException if an I/O error occurs
     */
    UserDTO deleteProfilePicture() throws IOException;
}