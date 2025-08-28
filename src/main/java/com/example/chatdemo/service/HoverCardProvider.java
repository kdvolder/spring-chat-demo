package com.example.chatdemo.service;

import java.util.Map;
import java.util.Optional;
import org.springframework.security.core.Authentication;

/**
 * Interface for providers of hover card information.
 * Implementations can be registered as Spring beans to contribute
 * hover card data for different types of users.
 * 
 * Providers are called during user authentication to pre-fetch and cache
 * hover card information, avoiding the need for API calls when displaying
 * hover cards later.
 */
public interface HoverCardProvider {
    
    /**
     * Provides hover card information for a user during authentication.
     * This method is called when a user logs in to pre-fetch their hover card data.
     * If this provider can't handle the given authentication, it should return an empty Optional.
     * 
     * @param authentication The authentication object from the security context
     * @param userInfo The basic user info already extracted from the authentication
     * @return A map of key-value pairs with hover card information,
     *         or empty Optional if this provider can't handle this authentication type
     */
    Optional<Map<String, String>> provideHoverCardInfo(
            Authentication authentication,
            UserInfoService.UserInfo userInfo);
}