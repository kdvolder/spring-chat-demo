package com.example.chatdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@SpringBootApplication
@EnableWebSocketMessageBroker
public class SpringChatDemoApplication {

    @Bean
    public WebSocketMessageBrokerConfigurer webSocketMessageBrokerConfigurer() {
        return new WebSocketMessageBrokerConfigurer() {
            @Override
            public void configureMessageBroker(MessageBrokerRegistry registry) {
                registry.enableSimpleBroker("/topic");
                registry.setApplicationDestinationPrefixes("/app");
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringChatDemoApplication.class, args);
    }
} 