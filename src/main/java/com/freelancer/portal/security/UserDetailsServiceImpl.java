package com.freelancer.portal.security;

import com.freelancer.portal.model.User;
import com.freelancer.portal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementation of Spring Security's UserDetailsService
 * Loads user details from the database using the UserRepository
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Attempting to load user by email: {}", username);
        
        try {
            // Start with case-insensitive search to ensure better matching
            return userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> {
                    log.error("User not found with email (case-insensitive): {}", username);
                    return new UsernameNotFoundException("User not found with email: " + username);
                });
        } catch (Exception e) {
            log.error("Error loading user by email: {}", username, e);
            throw new UsernameNotFoundException("Error finding user: " + e.getMessage());
        }
    }
}