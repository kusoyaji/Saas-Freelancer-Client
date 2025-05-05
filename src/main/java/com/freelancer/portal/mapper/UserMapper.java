package com.freelancer.portal.mapper;

import com.freelancer.portal.dto.UserDTO;
import com.freelancer.portal.dto.UserUpdateDto;
import com.freelancer.portal.model.User;

/**
 * Utility class for mapping User entities to DTOs and vice versa.
 */
public class UserMapper {

    /**
     * Maps a User entity to a UserDTO.
     *
     * @param user the user entity
     * @return the user DTO
     */
    public static UserDTO toDto(User user) {
        if (user == null) {
            return null;
        }
        
        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .profilePictureUrl(user.getProfilePictureUrl())
                .role(user.getRole())
                .bio(user.getBio())
                .website(user.getWebsite())
                .company(user.getCompany())
                .position(user.getPosition())
                .emailVerified(user.getEmailVerified())
                .accountLocked(user.getAccountLocked())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Updates a User entity from a UserUpdateDto.
     *
     * @param user the user entity to update
     * @param dto the DTO with new values
     * @return the updated user entity
     */
    public static User updateFromDto(User user, UserUpdateDto dto) {
        if (dto.getFirstName() != null) {
            user.setFirstName(dto.getFirstName());
        }
        
        if (dto.getLastName() != null) {
            user.setLastName(dto.getLastName());
        }
        
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        
        // Note: Password updates are typically handled by a service with proper encoding
        
        return user;
    }
}