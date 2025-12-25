package com.vendo.notification.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Configuration for real-time notifications
 * Enables STOMP over WebSocket for notification delivery
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker to carry messages back to the client
        // on destinations prefixed with "/topic"
        config.enableSimpleBroker("/topic");
        
        // Messages from client to server should be prefixed with "/app"
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register "/ws" endpoint for WebSocket connections
        // SockJS is used as a fallback for browsers that don't support WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // In production, specify exact origins
                .withSockJS();
        
        // Also register native WebSocket endpoint (without SockJS)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }
}

