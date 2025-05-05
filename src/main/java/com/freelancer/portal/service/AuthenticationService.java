package com.freelancer.portal.service;

import com.freelancer.portal.dto.AuthenticationRequest;
import com.freelancer.portal.dto.AuthenticationResponse;
import com.freelancer.portal.dto.RegisterRequest;
import org.springframework.security.core.Authentication;

/**
 * Service interface for authentication operations.
 */
public interface AuthenticationService {

    /**
     * Register a new user.
     *
     * @param request the registration request
     * @return the authentication response
     */
    AuthenticationResponse register(RegisterRequest request);

    /**
     * Authenticate a user.
     *
     * @param request the authentication request
     * @return the authentication response
     */
    AuthenticationResponse authenticate(AuthenticationRequest request);

    /**
     * Log out a user.
     *
     * @param authentication the authentication object
     */
    void logout(Authentication authentication);
}