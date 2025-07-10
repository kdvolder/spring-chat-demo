package com.example.chatdemo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import java.security.Principal;
import java.util.Collection;

@RestController
@RequestMapping("/api")
public class UserController {

    // Record to represent user information response
    public static record UserInfo(String username, Collection<String> roles) {}

    @GetMapping("/user")
    public UserInfo getCurrentUser(Principal principal, Authentication authentication) {
        String username = principal.getName();
        
        // Extract role names from authorities
        Collection<String> roles = authentication.getAuthorities().stream()
            .map(authority -> authority.getAuthority())
            .toList();
        
        return new UserInfo(username, roles);
    }
} 