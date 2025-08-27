package com.example.chatdemo.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * GitHub-specific implementation of HoverCardProvider.
 * Uses the current user's OAuth2 token to fetch real data from the GitHub API.
 * 
 * This demonstrates using the token from the deprecated
 * NimbusAuthorizationCodeTokenResponseClient to make API calls.
 */
@Component
public class GitHubHoverCardProvider implements HoverCardProvider {

    @Autowired
    private OAuth2AuthorizedClientService clientService;

    @Override
    public Optional<Map<String, String>> provideHoverCardInfo(
            String providerType,
            String providerName,
            String username,
            UserInfoService.UserInfo baseInfo) {
        
        // Check if this provider can handle the request
        if (!"oauth2".equals(providerType) || !"github".equals(providerName)) {
            System.out.println("❌ GitHub provider cannot handle: " + providerType + ":" + providerName);
            return Optional.empty();
        }
        
        System.out.println("🚀 GitHub hover card provider for: " + username);
        
        // Get the current authenticated user
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Check if the current user is authenticated with OAuth2 and specifically GitHub
        System.out.println("🔍 Checking current user authentication: " + 
                          (authentication != null ? authentication.getClass().getSimpleName() : "null"));
        
        if (authentication == null) {
            System.out.println("❌ No authentication found in SecurityContext");
            return Optional.empty();
        }
        
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            System.out.println("❌ Current user is not authenticated with OAuth2");
            System.out.println("   Authentication type: " + authentication.getClass().getSimpleName());
            return Optional.empty();
        }
        
        OAuth2AuthenticationToken oauth2Auth = (OAuth2AuthenticationToken) authentication;
        System.out.println("✅ Current user is authenticated with OAuth2");
        System.out.println("   Registration ID: " + oauth2Auth.getAuthorizedClientRegistrationId());
        
        if (!"github".equals(oauth2Auth.getAuthorizedClientRegistrationId())) {
            System.out.println("❌ Current user is not authenticated with GitHub");
            System.out.println("   Cannot provide GitHub hover card without a GitHub OAuth2 token");
            return Optional.empty();
        }
        
        System.out.println("✅ Current user is authenticated with GitHub OAuth2");
        
        // Get the OAuth2 client for the current user
        OAuth2AuthorizedClient client = null;
        try {
            String principalName = authentication.getName();
            System.out.println("🔑 Loading GitHub OAuth2 client for principal: " + principalName);
            
            client = clientService.loadAuthorizedClient("github", principalName);
            
            if (client == null) {
                System.out.println("⚠️ No GitHub OAuth2 client found for the current user");
                return Optional.empty();
            }
        } catch (Exception e) {
            System.out.println("⚠️ Could not load GitHub client: " + e.getMessage());
            return Optional.empty();
        }
        
        // Now use the current user's OAuth2 client to make an API call about the target user
        try {
            System.out.println("🔑 Using OAuth2 token from deprecated NimbusAuthorizationCodeTokenResponseClient");
            System.out.println("🔍 Making GitHub API call to get authenticated user's info");
            
            // Make API call to GitHub using the OAuth2 token
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(client.getAccessToken().getTokenValue());
            headers.set("Accept", "application/vnd.github.v3+json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // First try to get the authenticated user's own information
            // This will work regardless of the target username
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "https://api.github.com/user",
                HttpMethod.GET,
                entity,
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            Map<String, Object> userData = response.getBody();
            if (userData == null || userData.isEmpty()) {
                System.out.println("⚠️ GitHub API returned empty response");
                return Optional.empty();
            }
            
            // Extract useful information from the GitHub API response
            Map<String, String> details = new HashMap<>();
            details.put("Provider", "GitHub");
            
            if (userData.get("login") != null) {
                details.put("Username", "@" + userData.get("login").toString());
            }
            if (userData.get("name") != null) {
                details.put("Name", userData.get("name").toString());
            }
            if (userData.get("company") != null) {
                details.put("Company", userData.get("company").toString());
            }
            if (userData.get("location") != null) {
                details.put("Location", userData.get("location").toString());
            }
            if (userData.get("bio") != null) {
                details.put("Bio", userData.get("bio").toString());
            }
            if (userData.get("public_repos") != null) {
                details.put("Public Repos", userData.get("public_repos").toString());
            }
            if (userData.get("followers") != null) {
                details.put("Followers", userData.get("followers").toString());
            }
            
            System.out.println("✅ GitHub API call successful using OAuth2 token");
            System.out.println("👤 Retrieved info for authenticated user: " + userData.get("login"));
            System.out.println("📝 Note: In a real app, we would use this token to fetch info about: " + username);
            
            return Optional.of(details);
            
        } catch (Exception e) {
            System.out.println("❌ GitHub API call failed: " + e.getMessage());
            return Optional.empty();
        }
    }
}