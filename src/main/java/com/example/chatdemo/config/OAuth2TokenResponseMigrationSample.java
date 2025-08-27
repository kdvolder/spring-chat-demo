package com.example.chatdemo.config;

import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;

import java.util.HashMap;
import java.util.Map;


public class OAuth2TokenResponseMigrationSample {

    public OAuth2AccessTokenResponseHttpMessageConverter use_setTokenResponseConverter() {
        OAuth2AccessTokenResponseHttpMessageConverter converter = new OAuth2AccessTokenResponseHttpMessageConverter();
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
        converter.setTokenResponseParametersConverter(source -> {
            var objParams = source.getAdditionalParameters();
            Map<String, String> output = new HashMap<>();
            objParams.entrySet().forEach(e -> {
                if (e.getValue() instanceof String v) {
                    output.put(e.getKey(), v);
                }
            });
            return output;
        });
        return converter;
    }

    OAuth2AccessTokenResponseHttpMessageConverter use_setTokenResponseParamConverter() {
        OAuth2AccessTokenResponseHttpMessageConverter converter = new OAuth2AccessTokenResponseHttpMessageConverter();
        converter.setTokenResponseParametersConverter(source -> {
            var objParams = source.getAdditionalParameters();
            Map<String, String> output = new HashMap<>();
            objParams.entrySet().forEach(e -> {
                if (e.getValue() instanceof String v) {
                    output.put(e.getKey(), v);
                }
            });
            return output;
        });
        return converter;
    }
}
