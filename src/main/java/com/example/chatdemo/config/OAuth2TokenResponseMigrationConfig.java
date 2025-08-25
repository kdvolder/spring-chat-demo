package com.example.chatdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.web.client.RestTemplate;



@Configuration
public class OAuth2TokenResponseMigrationConfig {

    // This configures the DefaultAuthorizationCodeTokenResponseClient to use a RestTemplate
    // which includes our custom OAuth2AccessTokenResponseHttpMessageConverter.
    // This demonstrates the context in which the deprecated converter would have been used.
    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> authorizationCodeAccessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient client = new DefaultAuthorizationCodeTokenResponseClient();
        
        // Create custom converter with deprecated method - inline creation is cleaner
        OAuth2AccessTokenResponseHttpMessageConverter converter = new OAuth2AccessTokenResponseHttpMessageConverter();
        
        // DEPRECATED METHOD - REMOVED IN SPRING SECURITY 6.0
        // This method call will be replaced by our OpenRewrite recipe with:
        // converter.setAccessTokenResponseConverter(source -> { ... });
        // 
        // The replacement method has identical signature - just a name change!
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
            
            // In a real scenario, you might want to:
            // - Log token details for audit purposes
            // - Transform token scopes
            // - Add custom validation
            // - Set custom token expiration
            
            return OAuth2AccessTokenResponse.withToken(accessToken)
                    .tokenType(OAuth2AccessToken.TokenType.BEARER)
                    .scopes(scope != null ? java.util.Set.of(scope.split(" ")) : java.util.Set.of())
                    .build();
        });
        
        // Create a RestTemplate with all the default message converters plus our custom one
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(0, converter); // Add our converter first
        
        client.setRestOperations(restTemplate);
        return client;
    }
}
