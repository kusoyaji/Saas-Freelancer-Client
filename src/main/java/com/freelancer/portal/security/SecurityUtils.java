package com.freelancer.portal.security;

import com.freelancer.portal.exception.UnauthorizedException;
import com.freelancer.portal.model.User;
import com.freelancer.portal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility class for Spring Security operations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityUtils {

    private final UserRepository userRepository;

    /**
     * Get the logged-in user's username.
     *
     * @return the username of the logged-in user
     * @throws UnauthorizedException if no user is authenticated
     */
    public String getLoggedInUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            throw new UnauthorizedException("User not authenticated");
        }
        return authentication.getName();
    }

    /**
     * Check if a user is logged in.
     *
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        try {
            getLoggedInUsername();
            return true;
        } catch (UnauthorizedException e) {
            return false;
        }
    }

    /**
     * Get the current logged-in user.
     *
     * @return the current user entity
     * @throws UnauthorizedException if no user is authenticated
     */
    public User getCurrentUser() {
        String username = getLoggedInUsername();
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UnauthorizedException("User not found: " + username));
    }
    
    /**
     * Get the current logged-in user's ID.
     *
     * @return the current user's ID
     * @throws UnauthorizedException if no user is authenticated
     */
    public Long getCurrentUserId() {
        User currentUser = getCurrentUser();
        return currentUser.getId();
    }

    /**
     * Check if the current user has any of the specified roles.
     *
     * @param roles the roles to check
     * @return true if the user has any of the specified roles, false otherwise
     */
    public boolean hasAnyRole(String... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        for (String role : roles) {
            if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_" + role))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the current user has all of the specified roles.
     *
     * @param roles the roles to check
     * @return true if the user has all of the specified roles, false otherwise
     */
    public boolean hasAllRoles(String... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        for (String role : roles) {
            if (authentication.getAuthorities().stream()
                    .noneMatch(a -> a.getAuthority().equals("ROLE_" + role))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the current user is an admin.
     *
     * @return true if the user is an admin, false otherwise
     */
    public boolean isAdmin() {
        return hasAnyRole("ADMIN");
    }

    /**
     * Check if the current user is the owner of an entity or has admin privileges.
     *
     * @param resourceOwnerId the ID of the resource owner
     * @return true if the user is the owner or an admin, false otherwise
     */
    public boolean isOwnerOrAdmin(Long resourceOwnerId) {
        if (!isLoggedIn()) {
            return false;
        }
        if (isAdmin()) {
            return true;
        }
        User currentUser = getCurrentUser();
        return currentUser.getId().equals(resourceOwnerId);
    }

    public String getCurrentUsername(String username) {
        if (username != null ) {
            return getLoggedInUsername();
        }
        return username;
    }
}