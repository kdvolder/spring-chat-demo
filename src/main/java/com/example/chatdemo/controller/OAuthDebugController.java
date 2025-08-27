package com.example.chatdemo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Debug controller to show all OAuth2 attributes.
 * This helps understand what information is available from the OAuth2 provider.
 */
@RestController
@RequestMapping("/api/debug")
public class OAuthDebugController {

    /**
     * Dumps all OAuth2 attributes for the current user.
     * This is useful for understanding what information is available from the OAuth2 provider.
     */
    @GetMapping("/oauth-attributes")
    public Map<String, Object> dumpOAuthAttributes(Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        
        // Basic authentication info
        result.put("authenticated", authentication != null && authentication.isAuthenticated());
        result.put("authType", authentication != null ? authentication.getClass().getSimpleName() : "null");
        result.put("principal", authentication != null ? authentication.getName() : "null");
        
        // Check if this is OAuth2
        if (authentication instanceof OAuth2AuthenticationToken oauth2Auth) {
            result.put("oauth2Provider", oauth2Auth.getAuthorizedClientRegistrationId());
            
            // Get the OAuth2 user
            OAuth2User oauth2User = oauth2Auth.getPrincipal();
            
            // Add all attributes
            result.put("attributes", oauth2User.getAttributes());
            
            // Add authorities
            result.put("authorities", oauth2Auth.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .toList());
            
            // Add some common GitHub attributes for convenience
            Map<String, Object> githubInfo = new HashMap<>();
            
            // These are the most common GitHub attributes
            String[] commonAttrs = {
                "id", "login", "name", "avatar_url", "url", "html_url", "email"
            };
            
            for (String attr : commonAttrs) {
                githubInfo.put(attr, oauth2User.getAttribute(attr));
            }
            
            result.put("github", githubInfo);
        }
        
        return result;
    }
}
