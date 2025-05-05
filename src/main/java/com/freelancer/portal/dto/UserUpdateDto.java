package com.freelancer.portal.dto;

import com.freelancer.portal.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user update operations.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDto {
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;
    
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;
    
    @Email(message = "Invalid email format")
    private String email;
    private String phone;
    private String profilePictureUrl;
    private User.Role role;
    private String bio;
    private String website;
    private String company;
    private String position;
    private String password;
    
    private String oldPassword;
}