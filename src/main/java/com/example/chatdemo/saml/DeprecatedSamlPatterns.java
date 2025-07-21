package com.example.chatdemo.saml;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.saml2.provider.service.metadata.Saml2MetadataResolver;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.web.Saml2AuthenticationTokenConverter;
import org.springframework.security.saml2.provider.service.web.Saml2MetadataFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;

/**
 * REALISTIC examples of how SAML was configured in EARLY Spring Security 5.x
 * BEFORE RelyingPartyRegistrationResolver existed.
 * 
 * This shows the deprecated patterns our OpenRewrite recipe should migrate.
 */
@Configuration
public class DeprecatedSamlPatterns {

    /**
     * REALISTIC DEPRECATED PATTERN: Custom Converter implementation
     * 
     * In early Spring Security 5.x, users would create custom classes that
     * implemented Converter<HttpServletRequest, RelyingPartyRegistration>
     * to resolve the relying party registration from the request.
     */
    public static class CustomRelyingPartyResolver 
            implements Converter<HttpServletRequest, RelyingPartyRegistration> {
        
        private final RelyingPartyRegistrationRepository repository;
        
        public CustomRelyingPartyResolver(RelyingPartyRegistrationRepository repository) {
            this.repository = repository;
        }
        
        @Override
        public RelyingPartyRegistration convert(HttpServletRequest request) {
            // Custom logic to extract registration ID from request
            String registrationId = extractRegistrationId(request);
            return repository.findByRegistrationId(registrationId);
        }
        
        private String extractRegistrationId(HttpServletRequest request) {
            // Extract from path: /saml2/service-provider-metadata/{registrationId}
            String path = request.getRequestURI();
            if (path.contains("/saml2/service-provider-metadata/")) {
                return path.substring(path.lastIndexOf('/') + 1);
            }
            // Default fallback
            return "okta";
        }
    }

    /**
     * DEPRECATED PATTERN: Using custom Converter in SAML metadata filter
     * 
     * This is how the SAML metadata filter would have been configured
     * in early Spring Security 5.x using the custom Converter.
     */
    // @Bean  // Commented out to avoid conflicts with modern version
    public Saml2MetadataFilter deprecatedSaml2MetadataFilter(
            RelyingPartyRegistrationRepository repository,
            Saml2MetadataResolver metadataResolver) {
        
        // Using custom converter implementation - DEPRECATED pattern
        Converter<HttpServletRequest, RelyingPartyRegistration> customConverter = 
                new CustomRelyingPartyResolver(repository);
        
        // DEPRECATED: Constructor taking Converter (only this constructor existed in early Spring 5.x)
        return new Saml2MetadataFilter(customConverter, metadataResolver);
    }

    /**
     * DEPRECATED PATTERN: Using custom Converter in authentication token converter
     */
    // @Bean  // Commented out to avoid conflicts
    public Saml2AuthenticationTokenConverter deprecatedAuthenticationTokenConverter(
            RelyingPartyRegistrationRepository repository) {
        
        // Using custom converter implementation - DEPRECATED pattern
        Converter<HttpServletRequest, RelyingPartyRegistration> customConverter = 
                new CustomRelyingPartyResolver(repository);
        
        // DEPRECATED: Constructor taking Converter (only this constructor existed in early Spring 5.x)
        return new Saml2AuthenticationTokenConverter(customConverter);
    }

    /**
     * ANOTHER REALISTIC EXAMPLE: Multi-tenant SAML resolver
     * 
     * A more complex example showing how custom converters were used
     * for multi-tenant SAML configurations.
     */
    public static class MultiTenantSamlResolver 
            implements Converter<HttpServletRequest, RelyingPartyRegistration> {
        
        private final RelyingPartyRegistrationRepository repository;
        
        public MultiTenantSamlResolver(RelyingPartyRegistrationRepository repository) {
            this.repository = repository;
        }
        
        @Override
        public RelyingPartyRegistration convert(HttpServletRequest request) {
            // Extract tenant from subdomain or header
            String tenant = extractTenant(request);
            
            // Map tenant to registration ID
            String registrationId = mapTenantToRegistrationId(tenant);
            
            return repository.findByRegistrationId(registrationId);
        }
        
        private String extractTenant(HttpServletRequest request) {
            // Extract from subdomain: tenant1.example.com
            String serverName = request.getServerName();
            if (serverName.contains(".")) {
                return serverName.substring(0, serverName.indexOf('.'));
            }
            
            // Extract from custom header
            String tenantHeader = request.getHeader("X-Tenant-ID");
            if (tenantHeader != null) {
                return tenantHeader;
            }
            
            // Default tenant
            return "default";
        }
        
        private String mapTenantToRegistrationId(String tenant) {
            // Map tenant names to SAML registration IDs
            switch (tenant) {
                case "acme": return "acme-okta";
                case "widgets": return "widgets-azure";
                case "default": 
                default: return "okta";
            }
        }
    }

    /**
     * MIGRATION TARGET SUMMARY:
     * 
     * Our OpenRewrite recipe should detect and migrate:
     * 
     * 1. Classes implementing Converter<HttpServletRequest, RelyingPartyRegistration>
     *    → Should implement RelyingPartyRegistrationResolver instead
     * 
     * 2. Constructor calls using Converter parameters
     *    → Should use RelyingPartyRegistrationResolver parameters
     * 
     * 3. Variable types and method signatures using Converter
     *    → Should use RelyingPartyRegistrationResolver
     * 
     * The key insight: RelyingPartyRegistrationResolver is just a renamed interface
     * with the same method signature, so the migration is straightforward.
     * 
     * Kris says: No it is not! The signature is different (registrationId parameter in one but not the other)
     */
} 