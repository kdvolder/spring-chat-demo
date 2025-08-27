package com.example.chatdemo.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import com.example.chatdemo.service.UserInfoService;
import com.example.chatdemo.service.UserInfoService.UserInfo;

@Controller
public class ChatController {

    private final UserInfoService userInfoService;
    
    public ChatController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    /**
     * Chat message record - only contains content and sender ID
     * The client will need to fetch the sender's display name separately
     * using the /api/users/{userId}/public endpoint
     */
    static record ChatMessage(String content, String senderId) {}

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(ChatMessage message, Authentication authentication) {
        System.out.println("Received message: " + message);
        
        // Get and cache current user info in one call
        UserInfo userInfo = userInfoService.getCurrentUserInfo(authentication);
        
        // Create a new message with just the content and the authenticated user's ID
        // The client will need to fetch the display name separately
        return new ChatMessage(message.content(), userInfo.id());
    }
} 