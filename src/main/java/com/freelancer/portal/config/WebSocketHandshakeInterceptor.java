package com.freelancer.portal.config;

import java.util.Map;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {
        
        log.info("Performing handshake for WebSocket connection");
        
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpSession session = servletRequest.getServletRequest().getSession();
            attributes.put("sessionId", session.getId());
            
            // Log details about the request for debugging
            String uri = request.getURI().toString();
            log.info("WebSocket connection attempt to: {}", uri);
            
            // Extract query parameters if any
            String query = request.getURI().getQuery();
            if (query != null && query.contains("token=")) {
                log.info("Connection includes token parameter");
            } else {
                log.warn("No token parameter found in WebSocket connection request");
            }
        }
        
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        
        if (exception != null) {
            log.error("Error during WebSocket handshake: {}", exception.getMessage());
        } else {
            log.info("WebSocket handshake completed successfully");
        }
    }
}