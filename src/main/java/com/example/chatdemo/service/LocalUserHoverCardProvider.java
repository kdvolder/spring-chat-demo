package com.example.chatdemo.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.stereotype.Component;

/**
 * Default implementation of HoverCardProvider for local users.
 * This provides basic hover card information for users who authenticate
 * with username/password.
 */
@Component
public class LocalUserHoverCardProvider implements HoverCardProvider {

    @Override
    public Optional<Map<String, String>> provideHoverCardInfo(
            Authentication authentication,
            UserInfoService.UserInfo userInfo) {
        
        // Skip OAuth2 and SAML authentications - they have their own providers
        if (authentication instanceof OAuth2AuthenticationToken) {
            return Optional.empty();
        }
        
        if (authentication.getPrincipal() instanceof Saml2AuthenticatedPrincipal) {
            return Optional.empty();
        }
        
        // This is a local user authentication
        String username = authentication.getName();
        
        Map<String, String> details = new HashMap<>();
        details.put("Provider", "Local Authentication");
        details.put("Username", username);
        
        System.out.println("👤 Local user hover card provider for: " + username);
        
        // For demonstration purposes, provide basic user information
        details.put("Account Type", "Standard User");
        
        // Add some demo roles based on the username
        if ("admin".equalsIgnoreCase(username)) {
            details.put("Role", "Administrator");
        } else if ("alice".equalsIgnoreCase(username)) {
            details.put("Role", "Developer");
            details.put("Project", "Spring Chat Demo");
        } else if ("bob".equalsIgnoreCase(username)) {
            details.put("Role", "Tester");
            details.put("Project", "Spring Chat Demo");
        } else {
            details.put("Role", "User");
        }
        
        return Optional.of(details);
    }
}