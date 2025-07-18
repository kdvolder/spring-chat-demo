package com.example.chatdemo.saml;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.saml2.provider.service.metadata.Saml2MetadataResolver;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.web.Saml2MetadataFilter;

import javax.servlet.http.HttpServletRequest;

/**
 * Examples of deprecated SAML constructor patterns in Spring Security 5
 * that need migration to Spring Security 6.
 * 
 * TARGET FOR OPENREWRITE RECIPE:
 * - Detect: Saml2MetadataFilter(Converter<HttpServletRequest, RelyingPartyRegistration>, ...)
 * - Migrate: Change Converter to RelyingPartyRegistrationResolver 
 */
public class DeprecatedSamlPatterns {

    // DEPRECATED: Constructor with Converter<HttpServletRequest, RelyingPartyRegistration>
    // This pattern exists in Spring Security 5 but will be removed in Spring Security 6
    // 
    // NOTE: This method demonstrates the deprecated constructor pattern that our OpenRewrite recipe will target.
    // Currently commented out due to constructor ambiguity in Spring Security 5.7.11.
    // In the "migration backwards" step, we'll create a working version of this deprecated pattern.
    /*
    public Saml2MetadataFilter createMetadataFilterWithConverter(
            Converter<HttpServletRequest, RelyingPartyRegistration> relyingPartyRegistrationResolver,
            Saml2MetadataResolver metadataResolver) {
        
        // This constructor pattern is what our OpenRewrite recipe should detect and migrate
        return new Saml2MetadataFilter(relyingPartyRegistrationResolver, metadataResolver);
    }
    */
    
    // TODO: Implement working deprecated pattern for OpenRewrite recipe testing
} 