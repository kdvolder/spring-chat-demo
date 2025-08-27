package com.example.chatdemo.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.NimbusAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * OAuth2 Token Response Migration Configuration
 * 
 * This configuration demonstrates two different deprecated API scenarios that can be
 * switched using the 'oauth2.demo.scenario' property:
 * 
 * - scenario=nimbus: Demonstrates NimbusAuthorizationCodeTokenResponseClient deprecation (TNZ-54246)
 * - scenario=converter: Demonstrates setTokenResponseConverter deprecation (TNZ-54245)
 */
@Configuration
public class OAuth2TokenResponseMigrationConfig {

    /**
     * SCENARIO 1: NimbusAuthorizationCodeTokenResponseClient Deprecation (TNZ-54246)
     * 
     * Activated with: oauth2.demo.scenario=nimbus
     * 
     * This demonstrates the deprecated NimbusAuthorizationCodeTokenResponseClient
     * which has limited customization options and was deprecated in Spring Security 6.0+.
     */
    @Bean
    @ConditionalOnProperty(name = "oauth2.demo.scenario", havingValue = "nimbus", matchIfMissing = true)
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> nimbusTokenResponseClient() {
        System.out.println("🔧 NIMBUS SCENARIO ACTIVE: Using deprecated NimbusAuthorizationCodeTokenResponseClient (TNZ-54246)");
        System.out.println("   ↳ This client has limited customization options and will be migrated by OpenRewrite recipe");
        
        // DEPRECATED - Target for OpenRewrite recipe TNZ-54246
        NimbusAuthorizationCodeTokenResponseClient actualClient = new NimbusAuthorizationCodeTokenResponseClient();
        
        // Wrap the deprecated client with logging to observe its usage
        OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> loggingWrapper = 
            new LoggingOAuth2AccessTokenResponseClientWrapper(actualClient);
        
        System.out.println("✅ NimbusAuthorizationCodeTokenResponseClient configured with debug logging wrapper");
        return loggingWrapper;
    }
    
    /**
     * Logging wrapper for OAuth2AccessTokenResponseClient
     * 
     * This wrapper delegates to the actual client (NimbusAuthorizationCodeTokenResponseClient)
     * but logs all token exchange operations for debugging and demonstration purposes.
     */
    private static class LoggingOAuth2AccessTokenResponseClientWrapper 
            implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {
        
        private final OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> delegate;
        
        public LoggingOAuth2AccessTokenResponseClientWrapper(
                OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> delegate) {
            this.delegate = delegate;
        }
        
        @Override
        public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest) {
            System.out.println("\n🔄 === OAUTH2 TOKEN EXCHANGE STARTING ===");
            System.out.println("📋 Using DEPRECATED NimbusAuthorizationCodeTokenResponseClient (TNZ-54246)");
            System.out.println("🏢 Client Registration ID: " + authorizationGrantRequest.getClientRegistration().getRegistrationId());
            System.out.println("🔗 Authorization URI: " + authorizationGrantRequest.getClientRegistration().getProviderDetails().getAuthorizationUri());
            System.out.println("🎯 Token URI: " + authorizationGrantRequest.getClientRegistration().getProviderDetails().getTokenUri());
            System.out.println("📝 Authorization Code: " + authorizationGrantRequest.getAuthorizationExchange().getAuthorizationResponse().getCode().substring(0, 8) + "...");
            
            long startTime = System.currentTimeMillis();
            
            try {
                OAuth2AccessTokenResponse response = delegate.getTokenResponse(authorizationGrantRequest);
                long duration = System.currentTimeMillis() - startTime;
                
                System.out.println("✅ TOKEN EXCHANGE SUCCESSFUL (" + duration + "ms)");
                System.out.println("🎫 Access Token: " + response.getAccessToken().getTokenValue().substring(0, 10) + "...");
                System.out.println("⏰ Token Type: " + response.getAccessToken().getTokenType().getValue());
                System.out.println("🔄 Refresh Token: " + (response.getRefreshToken() != null ? "Present" : "Not provided"));
                System.out.println("🏷️  Scopes: " + response.getAccessToken().getScopes());
                
                // DEBUG: Show what GitHub actually sent us
                System.out.println("🔍 RAW TOKEN RESPONSE DEBUG:");
                System.out.println("   📊 Additional Parameters: " + response.getAdditionalParameters());
                if (response.getAdditionalParameters().containsKey("expires_in")) {
                    System.out.println("   ⏰ Raw expires_in from GitHub: " + response.getAdditionalParameters().get("expires_in"));
                } else {
                    System.out.println("   ❌ No expires_in field in GitHub response!");
                }
                
                // Show token expiry information
                if (response.getAccessToken().getIssuedAt() != null) {
                    System.out.println("📅 Issued At: " + response.getAccessToken().getIssuedAt());
                }
                if (response.getAccessToken().getExpiresAt() != null) {
                    System.out.println("⏰ Expires At: " + response.getAccessToken().getExpiresAt());
                    
                    java.time.Instant now = java.time.Instant.now();
                    java.time.Instant expiresAt = response.getAccessToken().getExpiresAt();
                    long secondsUntilExpiry = java.time.Duration.between(now, expiresAt).getSeconds();
                    
                    System.out.println("🕐 Current Time: " + now);
                    System.out.println("⌛ Expires In: " + secondsUntilExpiry + " seconds (" + (secondsUntilExpiry / 3600.0) + " hours)");
                    
                    if (secondsUntilExpiry < 60) {
                        System.out.println("⚠️  WARNING: Token expires very quickly! This seems unusual for GitHub.");
                    }
                } else {
                    System.out.println("⏰ Expires At: No expiry information provided (token may not expire)");
                }
                System.out.println("=== OAUTH2 TOKEN EXCHANGE COMPLETED ===\n");
                
                return response;
                
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                System.out.println("❌ TOKEN EXCHANGE FAILED (" + duration + "ms)");
                System.out.println("💥 Error: " + e.getMessage());
                System.out.println("=== OAUTH2 TOKEN EXCHANGE FAILED ===\n");
                throw e;
            }
        }
    }

    /**
     * SCENARIO 2: OAuth2AccessTokenResponseHttpMessageConverter.setTokenResponseConverter Deprecation (TNZ-54245)
     * 
     * Activated with: oauth2.demo.scenario=converter
     * 
     * This demonstrates the deprecated setTokenResponseConverter method which was
     * renamed to setAccessTokenResponseConverter in Spring Security 6.0+.
     */
    @Bean
    @ConditionalOnProperty(name = "oauth2.demo.scenario", havingValue = "converter")
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> converterTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient client = new DefaultAuthorizationCodeTokenResponseClient();
        
        // Create custom converter with deprecated method
        OAuth2AccessTokenResponseHttpMessageConverter converter = new OAuth2AccessTokenResponseHttpMessageConverter();
        
        // DEPRECATED METHOD - REMOVED IN SPRING SECURITY 6.0
        // This method call will be replaced by OpenRewrite recipe TNZ-54245 with:
        // converter.setAccessTokenResponseConverter(source -> { ... });
        converter.setTokenResponseConverter(source -> {
            System.out.println("=== DEPRECATED CODE PATH EXECUTED ===");
            System.out.println("Processing GitHub OAuth2 token response using deprecated setTokenResponseConverter method");
            System.out.println("Source data keys: " + source.keySet());
            
            // Extract actual token details from the source map for a realistic response
            String accessToken = (String) source.get("access_token");
            String tokenType = (String) source.get("token_type");
            String scope = (String) source.get("scope");
            
            System.out.println("Extracted access_token (first 10 chars): " + 
                (accessToken != null ? accessToken.substring(0, Math.min(10, accessToken.length())) + "..." : "null"));
            System.out.println("Token type: " + tokenType);
            System.out.println("Scope: " + scope);
            
            return OAuth2AccessTokenResponse.withToken(accessToken)
                    .tokenType(OAuth2AccessToken.TokenType.BEARER)
                    .scopes(scope != null ? java.util.Set.of(scope.split(" ")) : java.util.Set.of())
                    .build();
        });
        
        // Create a RestTemplate with the custom converter
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(0, converter);
        
        client.setRestOperations(restTemplate);
        return client;
    }
}
