package com.freelancer.portal.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Adapter class that provides JWT token functionality for WebSocket authentication
 * using the existing JwtService
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Extract username from JWT token.
     *
     * @param token the JWT token
     * @return the username
     */
    public String getUsernameFromJWT(String token) {
        return jwtService.extractUsername(token);
    }

    /**
     * Validate a JWT token.
     *
     * @param token the JWT token
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            String username = jwtService.extractUsername(token);
            if (username == null) {
                log.warn("Username is null in token");
                return false;
            }
            
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return jwtService.isTokenValid(token, userDetails);
        } catch (Exception e) {
            log.error("Failed to validate token", e);
            return false;
        }
    }
}