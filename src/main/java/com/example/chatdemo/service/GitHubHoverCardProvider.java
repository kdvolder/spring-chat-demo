package com.example.chatdemo.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * GitHub-specific implementation of HoverCardProvider.
 * Uses the user's OAuth2 token to fetch real data from the GitHub API during authentication.
 * 
 * This demonstrates using the token from the deprecated
 * NimbusAuthorizationCodeTokenResponseClient to make API calls.
 * 
 * The data is fetched once during authentication and then cached for later use.
 */
@Component
public class GitHubHoverCardProvider implements HoverCardProvider {

    @Autowired
    private OAuth2AuthorizedClientService clientService;

    @Override
    public Optional<Map<String, String>> provideHoverCardInfo(
            Authentication authentication,
            UserInfoService.UserInfo userInfo) {
        
        System.out.println("🚀 GitHub hover card provider checking authentication");
        
        // Check if the authentication is OAuth2 and specifically GitHub
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2Auth)) {
            System.out.println("❌ Not an OAuth2 authentication: " + authentication.getClass().getSimpleName());
            return Optional.empty();
        }
        
        if (!"github".equals(oauth2Auth.getAuthorizedClientRegistrationId())) {
            System.out.println("❌ Not a GitHub OAuth2 authentication: " + oauth2Auth.getAuthorizedClientRegistrationId());
            return Optional.empty();
        }
        
        System.out.println("✅ GitHub OAuth2 authentication detected");
        
        // Get the OAuth2 client directly from the authentication
        OAuth2AuthorizedClient client = null;
        try {
            String principalName = authentication.getName();
            System.out.println("🔑 Loading GitHub OAuth2 client for principal: " + principalName);
            
            client = clientService.loadAuthorizedClient("github", principalName);
            
            if (client == null) {
                System.out.println("⚠️ No GitHub OAuth2 client found");
                return Optional.empty();
            }
        } catch (Exception e) {
            System.out.println("⚠️ Could not load GitHub client: " + e.getMessage());
            return Optional.empty();
        }
        
        // Use the OAuth2 client to make an API call
        try {
            System.out.println("🔑 Using OAuth2 token from deprecated NimbusAuthorizationCodeTokenResponseClient");
            System.out.println("🔍 Making GitHub API call to get authenticated user's info");
            
            // Make API call to GitHub using the OAuth2 token
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(client.getAccessToken().getTokenValue());
            headers.set("Accept", "application/vnd.github.v3+json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // Get the authenticated user's own information
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
            
            return Optional.of(details);
            
        } catch (Exception e) {
            System.out.println("❌ GitHub API call failed: " + e.getMessage());
            return Optional.empty();
        }
    }
}