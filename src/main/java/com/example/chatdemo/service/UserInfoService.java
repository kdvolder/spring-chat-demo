package com.example.chatdemo.service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.stereotype.Service;

/**
 * Service to provide consistent user information across different authentication types.
 * Handles the extraction of user identity and display information from various authentication providers.
 * 
 * This service enforces strict authentication boundaries by throwing exceptions for unauthenticated access.
 * It also maintains an in-memory cache of user display information to support lookups by secure ID.
 */
@Service
public class UserInfoService {
    // In-memory cache of user info by secure ID
    // In a real application, this would be backed by a database
    private final Map<String, UserInfo> userCache = new ConcurrentHashMap<>();
    
    /**
     * Creates or retrieves a standardized user info record with identity and display information.
     * Also caches the user info for later lookup by ID.
     * 
     * This is the primary method for getting user information from an Authentication object.
     * 
     * @throws AuthenticationCredentialsNotFoundException if authentication is null
     */
    public UserInfo getCurrentUserInfo(Authentication authentication) {
        if (authentication == null) {
            throw new AuthenticationCredentialsNotFoundException(
                "Authentication required. Anonymous access not permitted.");
        }
        
        // Extract secure ID
        String secureId = generateSecureId(authentication);
        
        // Extract role names from authorities
        Collection<String> roles = authentication.getAuthorities().stream()
            .map(authority -> authority.getAuthority())
            .toList();
        
        // Create user info object
        UserInfo userInfo = new UserInfo(
            secureId,
            extractDisplayName(authentication),
            roles,
            extractAvatarUrl(authentication)
        );
        
        // Cache the user info for later lookup
        userCache.put(secureId, userInfo);
        
        return userInfo;
    }
    
    /**
     * Looks up user info by secure ID.
     * 
     * @param secureId The secure ID of the user to look up
     * @return The user info, or null if not found
     */
    public UserInfo getUserInfoById(String secureId) {
        return userCache.get(secureId);
    }
    
    /**
     * Looks up public user info by secure ID.
     * This method only returns non-sensitive information suitable for sharing with other users.
     * 
     * @param secureId The secure ID of the user to look up
     * @return The public user info, or null if not found
     */
    public PublicUserInfo getPublicUserInfoById(String secureId) {
        UserInfo userInfo = userCache.get(secureId);
        if (userInfo == null) {
            return null;
        }
        return PublicUserInfo.fromUserInfo(userInfo);
    }
    
    /**
     * Standard user info record that works across all authentication types
     */
    public record UserInfo(
        String id,                  // Secure, qualified ID (e.g., "oauth2:github:1002156")
        String displayName,         // Human-readable name (e.g., "Kris De Volder")
        Collection<String> roles,   // User's security roles/authorities
        String avatarUrl            // URL to user's profile picture (if available)
    ) {}
    
    /**
     * Public user info record with only non-sensitive information
     * suitable for sharing with other users in the chat application.
     */
    public record PublicUserInfo(
        String id,                  // Secure, qualified ID (e.g., "oauth2:github:1002156")
        String displayName,         // Human-readable name (e.g., "Kris De Volder")
        String avatarUrl            // URL to user's profile picture (if available)
    ) {
        /**
         * Creates a public user info from a complete user info
         */
        public static PublicUserInfo fromUserInfo(UserInfo userInfo) {
            return new PublicUserInfo(
                userInfo.id(),
                userInfo.displayName(),
                userInfo.avatarUrl()
            );
        }
    }
    
    // Private helper methods
    
    /**
     * Generates a globally unique, secure ID for the user that includes their auth provider
     * to prevent ID collisions across different authentication systems.
     */
    private String generateSecureId(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken oauth2Auth) {
            String provider = oauth2Auth.getAuthorizedClientRegistrationId();
            return "oauth2:" + provider + ":" + authentication.getName();
        }
        
        if (authentication.getPrincipal() instanceof Saml2AuthenticatedPrincipal) {
            return "saml:" + authentication.getName();
        }
        
        return "local:" + authentication.getName();
    }
    
    /**
     * Extracts a display-friendly name for any authenticated user
     */
    private String extractDisplayName(Authentication authentication) {
        // OAuth2 login (e.g., GitHub)
        if (authentication instanceof OAuth2AuthenticationToken oauth2Auth) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String provider = oauth2Auth.getAuthorizedClientRegistrationId();
            
            if ("github".equals(provider)) {
                // GitHub-specific logic
                String name = oauth2User.getAttribute("name");
                return name != null ? name : oauth2User.getName();
            }
            
            // Generic OAuth2 fallback
            return oauth2User.getName();
        }
        
        // SAML login
        if (authentication.getPrincipal() instanceof Saml2AuthenticatedPrincipal samlUser) {
            String name = samlUser.getFirstAttribute("displayName");
            return name != null ? name : samlUser.getName();
        }
        
        // Regular username/password (UserDetails)
        return authentication.getName();
    }
    
    /**
     * Extracts avatar URL for the authenticated user if available
     */
    private String extractAvatarUrl(Authentication authentication) {
        // OAuth2 login (e.g., GitHub)
        if (authentication instanceof OAuth2AuthenticationToken oauth2Auth) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String provider = oauth2Auth.getAuthorizedClientRegistrationId();
            
            if ("github".equals(provider)) {
                // GitHub provides avatar_url in user attributes
                return oauth2User.getAttribute("avatar_url");
            }
            
            // For other OAuth2 providers, look for common attribute names
            String avatarUrl = oauth2User.getAttribute("picture"); // Google, Facebook
            if (avatarUrl == null) {
                avatarUrl = oauth2User.getAttribute("avatar"); // Some providers
            }
            return avatarUrl;
        }
        
        // SAML login - try common attribute names
        if (authentication.getPrincipal() instanceof Saml2AuthenticatedPrincipal samlUser) {
            String avatarUrl = samlUser.getFirstAttribute("picture");
            if (avatarUrl == null) {
                avatarUrl = samlUser.getFirstAttribute("avatar");
            }
            return avatarUrl;
        }
        
        // Default - no avatar URL available
        return null;
    }
}
