package com.example.chatdemo.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
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
    
    // In-memory cache of hover card info by secure ID
    private final Map<String, UserHoverCardInfo> hoverCardCache = new ConcurrentHashMap<>();
    
    @Autowired
    private List<HoverCardProvider> hoverCardProviders;
    
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
        
        // Pre-fetch and cache hover card info during authentication
        prefetchHoverCardInfo(secureId, authentication, userInfo);
        
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
     * Gets hover card information for a user by their secure ID.
     * This is used to display additional information when hovering over a username in chat.
     * 
     * @param secureId The secure ID of the user to look up
     * @return The hover card info, or null if not found
     */
    public UserHoverCardInfo getHoverCardInfoById(String secureId) {
        System.out.println("🔍 Getting hover card for user ID: " + secureId);
        
        // First check if we have pre-cached hover card info
        UserHoverCardInfo cachedInfo = hoverCardCache.get(secureId);
        if (cachedInfo != null) {
            System.out.println("✅ Found pre-cached hover card info for: " + cachedInfo.displayName());
            return cachedInfo;
        }
        
        System.out.println("🔄 No cached hover card info found, checking user cache");
        System.out.println("🗄️ Current user cache entries: " + userCache.keySet());
        
        UserInfo userInfo = userCache.get(secureId);
        if (userInfo == null) {
            System.out.println("❌ User not found in cache: " + secureId);
            return null; // No hover card for unknown users
        }
        
        System.out.println("✅ User found in cache: " + userInfo.displayName());
        System.out.println("ℹ️ No hover card details available for: " + secureId);
        
        // Create a basic hover card with no details
        // We parse the ID to get the provider type
        String providerType = parseProviderType(secureId);
        
        UserHoverCardInfo basicInfo = new UserHoverCardInfo(
            userInfo.id(),
            userInfo.displayName(),
            userInfo.avatarUrl(),
            providerType,
            new HashMap<>() // Empty details
        );
        
        // Cache this basic info to avoid repeated lookups
        hoverCardCache.put(secureId, basicInfo);
        
        return basicInfo;
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
    
    /**
     * User hover card information - contains additional details about a user
     * that can be displayed when hovering over their name in the chat.
     */
    public record UserHoverCardInfo(
        String id,                  // Secure, qualified ID (e.g., "oauth2:github:1002156")
        String displayName,         // Human-readable name (e.g., "Kris De Volder")
        String avatarUrl,           // URL to user's profile picture (if available)
        String providerType,        // Authentication provider (e.g., "github", "saml", "local")
        Map<String, String> details // Additional provider-specific details (key-value pairs)
    ) {}
    
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
     * Pre-fetches hover card information during authentication and caches it
     * This is called during the initial authentication flow to ensure we have
     * hover card data ready when needed, without having to make API calls later.
     */
    private void prefetchHoverCardInfo(String secureId, Authentication authentication, UserInfo userInfo) {
        System.out.println("🔄 Pre-fetching hover card info for: " + secureId);
        
        // Parse the user ID components for the hover card info
        String providerType = parseProviderType(secureId);
        
        // Create basic hover card info
        Map<String, String> details = new HashMap<>();
        
        // Try to find a provider that can handle this user
        boolean providerFound = false;
        System.out.println("🔍 Looking for hover card providers during authentication");
        System.out.println("   Authentication type: " + authentication.getClass().getSimpleName());
        System.out.println("   Available providers: " + hoverCardProviders.size());
        
        // Pass the authentication object directly to providers
        for (HoverCardProvider provider : hoverCardProviders) {
            System.out.println("   Trying provider: " + provider.getClass().getSimpleName());
            
            Optional<Map<String, String>> providerDetails = 
                provider.provideHoverCardInfo(authentication, userInfo);
            
            if (providerDetails.isPresent()) {
                System.out.println("🔌 Using provider: " + provider.getClass().getSimpleName());
                details.putAll(providerDetails.get());
                providerFound = true;
                System.out.println("   Details found: " + details.size() + " entries");
                break; // Use the first provider that returns details
            } else {
                System.out.println("   Provider returned no details");
            }
        }
        
        // Create and cache the hover card info
        UserHoverCardInfo hoverCardInfo = new UserHoverCardInfo(
            userInfo.id(),
            userInfo.displayName(),
            userInfo.avatarUrl(),
            providerType,
            details
        );
        
        hoverCardCache.put(secureId, hoverCardInfo);
        
        if (providerFound) {
            System.out.println("✅ Successfully pre-cached hover card info for: " + userInfo.displayName());
        } else {
            System.out.println("ℹ️ Pre-cached basic hover card info without details for: " + userInfo.displayName());
        }
    }
    
    /**
     * Parse the provider type from a secure ID
     */
    private String parseProviderType(String secureId) {
        if (secureId.contains(":")) {
            return secureId.substring(0, secureId.indexOf(":"));
        }
        return "unknown";
    }
    
    // These methods are kept for future use if needed
    // Currently not used since we're passing the Authentication object directly to providers
    
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