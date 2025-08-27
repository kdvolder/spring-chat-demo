package com.example.chatdemo.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

/**
 * SAML-specific implementation of HoverCardProvider.
 * In a real application, this would use SAML attributes to provide
 * more detailed information about the user.
 */
@Component
public class SamlHoverCardProvider implements HoverCardProvider {

    @Override
    public Optional<Map<String, String>> provideHoverCardInfo(
            String providerType,
            String providerName,
            String username,
            UserInfoService.UserInfo baseInfo) {
        
        // Check if this provider can handle the request
        if (!"saml".equals(providerType)) {
            return Optional.empty();
        }
        
        Map<String, String> details = new HashMap<>();
        details.put("Provider", "SAML");
        
        // In a real application, this is where we would use SAML attributes
        // to provide more detailed information about the user
        
        System.out.println("🔐 SAML hover card provider for: " + username);
        
        // For demonstration purposes, provide realistic SAML-like data
        details.put("Organization", "Enterprise SSO");
        details.put("Department", "Engineering");
        details.put("Role", "Developer");
        details.put("Email Domain", username.contains("@") ? username.substring(username.indexOf("@") + 1) : "example.org");
        
        return Optional.of(details);
    }
}