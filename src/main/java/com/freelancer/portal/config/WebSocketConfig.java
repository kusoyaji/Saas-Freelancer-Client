package com.freelancer.portal.config;

import com.freelancer.portal.config.websocket.TokenHandshakeInterceptor;
import com.freelancer.portal.config.websocket.WebSocketAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.config.ChannelRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Autowired
    private  TokenHandshakeInterceptor tokenHandshakeInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable a simple in-memory message broker
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefix for messages sent from clients to the server
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix for user-specific messages
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Main WebSocket endpoint with SockJS fallback
        registry.addEndpoint("/api/v1/ws")
                .setAllowedOriginPatterns("http://localhost:4200")
                .addInterceptors(tokenHandshakeInterceptor)
                .withSockJS();
        
        // Single consolidated endpoint for timers and notifications
        // Remove separate timer endpoint to avoid URL format issues
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:4200")
                .addInterceptors(tokenHandshakeInterceptor)
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Add authentication interceptor for WebSocket messages
        registration.interceptors(webSocketAuthInterceptor);
    }
}