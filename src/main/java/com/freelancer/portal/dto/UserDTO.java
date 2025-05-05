package com.freelancer.portal.dto;

import com.freelancer.portal.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for user information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String profilePictureUrl;
    private User.Role role;
    private String bio;
    private String website;
    private String company;
    private String position;
    private Boolean emailVerified;
    private Boolean accountLocked;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Convert User entity to UserDTO
     * 
     * @param user the user entity
     * @return the user DTO
     */
    public static UserDTO fromEntity(User user) {
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
}