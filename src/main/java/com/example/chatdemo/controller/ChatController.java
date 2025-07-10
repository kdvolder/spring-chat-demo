package com.example.chatdemo.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    static record ChatMessage(String content, String sender) {}

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(ChatMessage message, Principal principal) {
        System.out.println("Received message: " + message);
        return new ChatMessage(message.content(), principal.getName());
    }
} 