package com.freelancer.portal.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        
        // Log the request path for debugging
        log.debug("Processing request to: " + request.getRequestURI());
        
        // Skip authentication for permitted paths
        if (shouldSkipAuthentication(request)) {
            log.debug("Skipping authentication for permitted path: " + request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }
        
        // Check if auth header is present
        if (authHeader == null || authHeader.isEmpty()) {
            log.debug("No Authorization header found");
            filterChain.doFilter(request, response);
            return;
        }
        
        log.debug("Auth header found: " + authHeader);
        
        // More tolerant extraction - trim and handle case where no space exists
        String authHeaderTrimmed = authHeader.trim();
        if (authHeaderTrimmed.toLowerCase().startsWith("bearer")) {
            // Extract token after "bearer" regardless of spacing
            jwt = authHeaderTrimmed.substring(6).trim();
        } else {
            // If no "bearer" prefix, try to use the whole header as token
            jwt = authHeaderTrimmed;
        }
        
        // Verify the JWT is not empty
        if (jwt.isEmpty()) {
            log.debug("JWT token is empty");
            filterChain.doFilter(request, response);
            return;
        }
        
        log.debug("Extracted JWT token (partial): " + jwt.substring(0, Math.min(jwt.length(), 10)) + "...");
        
        try {
            userEmail = jwtService.extractUsername(jwt);
            log.debug("Extracted username from token: " + userEmail);
            
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                log.debug("Loaded user details for: " + userEmail);
                
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    log.debug("JWT token is valid");
                    
                    // Extract user authorities from UserDetails
                    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
                    
                    // Log roles for debugging
                    logRoles(authorities);
                    
                    // Create authentication token with authorities
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            authorities
                    );
                    
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Authentication set in SecurityContext with authorities: " + authorities);
                } else {
                    log.debug("JWT token is not valid");
                }
            }
        } catch (Exception e) {
            // Log token parsing errors but don't block the request
            log.error("Invalid JWT token: " + e.getMessage(), e);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private void logRoles(Collection<? extends GrantedAuthority> authorities) {
        if (authorities != null && !authorities.isEmpty()) {
            authorities.forEach(authority -> 
                log.debug("User has authority: " + authority.getAuthority())
            );
        } else {
            log.debug("User has no authorities");
        }
    }
    
    private boolean shouldSkipAuthentication(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Skip authentication for auth endpoints
        if (path.startsWith("/auth/") ||
            path.contains("/auth/") ||
            path.equals("/api/v1/auth/login") ||
            path.startsWith("/api/v1/auth/") ||
            path.contains("/api/v1/auth/")) {
            return true;
        }
        
        // Skip authentication for WebSocket endpoints
        if (path.startsWith("/api/v1/ws") ||
            path.startsWith("/ws") ||
            path.contains("/topic/") ||
            path.contains("/queue/") ||
            path.contains("/app/") ||
            path.contains("/user/")) {
            return true;
        }
        
        // Skip authentication for API docs and error pages
        return path.contains("/v3/api-docs") || 
               path.contains("/swagger-ui") || 
               path.equals("/error");
    }
}