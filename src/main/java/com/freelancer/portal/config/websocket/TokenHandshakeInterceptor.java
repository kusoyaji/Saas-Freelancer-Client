package com.freelancer.portal.config.websocket;

import com.freelancer.portal.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Interceptor that validates JWT tokens during WebSocket handshake
 */
@Component
@Slf4j
public class TokenHandshakeInterceptor implements HandshakeInterceptor {
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        
        String query = request.getURI().getQuery();
        log.debug("WebSocket handshake attempt, query: {}", query);
        
        // First try to get token from query parameters
        String token = extractTokenFromQuery(query);
        
        // If no token in query, check headers
        if (!StringUtils.hasText(token)) {
            token = extractTokenFromHeaders(request);
        }
        
        // Validate the token if present
        if (StringUtils.hasText(token)) {
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    // If token is valid, store username in attributes for later use
                    String username = jwtTokenProvider.getUsernameFromJWT(token);
                    attributes.put("username", username);
                    log.info("WebSocket connection authenticated for user: {}", username);
                    return true;
                } else {
                    log.warn("WebSocket connection rejected: token validation failed");
                }
            } catch (Exception e) {
                log.error("WebSocket authentication error: {}", e.getMessage());
            }
        } else {
            log.warn("WebSocket connection attempted without token");
        }
        
        // For development purposes, allow connections even without valid token
        // Comment this out in production
        // log.warn("WebSocket connection allowed despite invalid token (DEVELOPMENT MODE)");
        // return true;
        
        log.warn("WebSocket connection rejected: invalid or missing token");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                              WebSocketHandler wsHandler, Exception exception) {
        // Nothing to do after handshake
    }
    
    /**
     * Extract the token from the query string
     */
    private String extractTokenFromQuery(String query) {
        if (query != null && query.contains("token=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    return param.substring(6); // "token=".length()
                }
            }
        }
        return null;
    }

    /**
     * Extract the token from the headers
     */
    private String extractTokenFromHeaders(ServerHttpRequest request) {
        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String bearerToken = authHeaders.get(0);
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7); // "Bearer ".length()
            }
        }
        return null;
    }
}