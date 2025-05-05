package com.freelancer.portal.dto;

import com.freelancer.portal.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Valid email format is required")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    private User.Role role;
    
    // Additional optional fields
    @Size(max = 15, message = "Phone number cannot exceed 15 characters")
    private String phone;
    
    @Size(max = 2000, message = "Bio cannot exceed 2000 characters")
    private String bio;
    
    @Size(max = 255, message = "Website URL cannot exceed 255 characters")
    private String website;
    
    @Size(max = 100, message = "Company name cannot exceed 100 characters")
    private String company;
    
    @Size(max = 100, message = "Position cannot exceed 100 characters")
    private String position;
    
    private String profilePictureUrl;
}