package com.freelancer.portal.service.impl;

import com.freelancer.portal.dto.AuthenticationRequest;
import com.freelancer.portal.dto.AuthenticationResponse;
import com.freelancer.portal.dto.RegisterRequest;
import com.freelancer.portal.dto.UserDTO;
import com.freelancer.portal.model.User;
import com.freelancer.portal.repository.UserRepository;
import com.freelancer.portal.security.JwtService;
import com.freelancer.portal.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    
    /**
     * Register a new user.
     */
    @Override
    public AuthenticationResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());
        
        // Build user with more complete information from the request
        User.UserBuilder builder = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : User.Role.USER)
                .emailVerified(false)
                .accountLocked(false);
        
        // Add optional fields if they are provided in the request
        if (request.getPhone() != null) {
            builder.phone(request.getPhone());
        }
        
        if (request.getBio() != null) {
            builder.bio(request.getBio());
        }
        
        if (request.getWebsite() != null) {
            builder.website(request.getWebsite());
        }
        
        if (request.getCompany() != null) {
            builder.company(request.getCompany());
        }
        
        if (request.getPosition() != null) {
            builder.position(request.getPosition());
        }
        
        if (request.getProfilePictureUrl() != null) {
            builder.profilePictureUrl(request.getProfilePictureUrl());
        }
        
        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        builder.createdAt(now);
        builder.updatedAt(now);
        
        User user = builder.build();
        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getEmail());
        
        String token = jwtService.generateToken(user);
        
        // Convert User entity to DTO
        UserDTO userDTO = UserDTO.fromEntity(user);
        
        // Build and return response with token and user info
        return AuthenticationResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400L) // 24 hours in seconds
                .user(userDTO)
                .build();
    }
    
    /**
     * Authenticate a user.
     */
    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("Authenticating user: {}", request.getEmail());
        
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        
        // Use case-insensitive search to ensure we find the user regardless of case
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + request.getEmail()));
        
        // Update last login timestamp
        user.setLastLogin(LocalDateTime.now());
        user = userRepository.save(user);
        
        log.info("User authenticated successfully: {}", user.getEmail());
        
        String token = jwtService.generateToken(user);
        
        // Convert User entity to DTO
        UserDTO userDTO = UserDTO.fromEntity(user);
        
        // Build and return response with token and user info
        return AuthenticationResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400L) // 24 hours in seconds
                .user(userDTO)
                .build();
    }
    
    @Override
    public void logout(Authentication authentication) {
        log.info("Logging out user: {}", authentication.getName());
        // JWT is stateless, so we don't need to invalidate anything server-side
        // In a real app, you might want to add the token to a blocklist
    }
}