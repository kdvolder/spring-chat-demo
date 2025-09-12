package com.example.chatdemo.config;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.MappedJwtClaimSetConverter;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoderJwkSupport;
import org.springframework.web.client.RestTemplate;

//@Configuration disabled, it is just an example
public class NimbusJwtDecoderJwkSupportExample {

    // This method demonstrates the deprecated NimbusJwtDecoderJwkSupport
    // In Spring Security 5.x, NimbusJwtDecoderJwkSupport was used with its constructor
    @Bean
    public NimbusJwtDecoderJwkSupport deprecatedJwtDecoder() {
        String jwkSetUri = "https://example.com/.well-known/jwks.json";
        return new NimbusJwtDecoderJwkSupport(jwkSetUri);
    }

    // This method demonstrates the modern replacement using NimbusJwtDecoder
    @Bean
    public JwtDecoder modernJwtDecoder() {
        String jwkSetUri = "https://example.com/.well-known/jwks.json";
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    // Test method to explore API differences between old and new types
    public void exploreApiDifferences() {
        String jwkSetUri = "https://example.com/.well-known/jwks.json";
        
        // Old deprecated type
        NimbusJwtDecoderJwkSupport oldDecoder = new NimbusJwtDecoderJwkSupport(jwkSetUri);
        
        // New replacement type  
        NimbusJwtDecoder newDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        
        // Both implement JwtDecoder interface, so decode() should work on both
        // Jwt jwt = oldDecoder.decode("some.jwt.token");
        // Jwt jwt2 = newDecoder.decode("some.jwt.token");
        
        // Test: Do both support JWT validation configuration?
        // These method calls will reveal API differences...
        
        // Test potential methods that might exist on the old type:
        testOldTypeSpecificMethods(oldDecoder);
        
        // Test potential methods that might exist on the new type:
        testNewTypeSpecificMethods(newDecoder);
    }
    
    private void testOldTypeSpecificMethods(NimbusJwtDecoderJwkSupport decoder) {
        // Test methods that exist on old type - these all work
        decoder.setJwtValidator(JwtValidators.createDefault());
        decoder.setClaimSetConverter(MappedJwtClaimSetConverter.withDefaults(Collections.emptyMap()));
        
        // CRITICAL: This method ONLY exists on NimbusJwtDecoderJwkSupport
        // It does NOT exist on NimbusJwtDecoder - this is the API incompatibility!
        RestTemplate customRestTemplate = new RestTemplate();
        decoder.setRestOperations(customRestTemplate);
    }
    
    private void testNewTypeSpecificMethods(NimbusJwtDecoder decoder) {
        // Test methods that exist on new type - these work
        decoder.setJwtValidator(JwtValidators.createDefault());
        decoder.setClaimSetConverter(MappedJwtClaimSetConverter.withDefaults(Collections.emptyMap()));
        
        // The setRestOperations method does NOT exist on NimbusJwtDecoder
        // decoder.setRestOperations(customRestTemplate); // COMPILATION ERROR!
        
        // Instead, RestOperations must be configured via the builder pattern:
        // NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
        //     .restOperations(customRestTemplate)  // Configure here during build
        //     .build();
    }

    // This method demonstrates the convenience helper using JwtDecoders
    @Bean
    public JwtDecoder convenienceJwtDecoder() {
        String issuerUri = "https://example.com";
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }
}
