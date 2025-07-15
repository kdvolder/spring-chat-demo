package com.example.chatdemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
            // Allow connection (CONNECT/DISCONNECT) - these don't need session context
            .simpTypeMatchers(SimpMessageType.CONNECT, SimpMessageType.DISCONNECT, SimpMessageType.HEARTBEAT)
                .permitAll()
            
            // Allow subscriptions to topics (users need to receive messages)
            .simpSubscribeDestMatchers("/topic/**")
                .authenticated()
            
            // Allow sending to application endpoints (controllers)
            .simpDestMatchers("/app/**")
                .authenticated()
            
            // BLOCK direct sends to topic destinations (prevents controller bypass)
            .simpDestMatchers("/topic/**")
                .denyAll()
            
            // Deny everything else
            .anyMessage()
                .denyAll();
    }

    @Override
    protected boolean sameOriginDisabled() {
        // Disable CSRF protection for WebSocket messages to prevent NPE
        // The HttpSessionCsrfTokenRepository tries to access null session in WebSocket context
        // We rely on authentication + our message-level authorization rules instead
        return true;
    }
} 