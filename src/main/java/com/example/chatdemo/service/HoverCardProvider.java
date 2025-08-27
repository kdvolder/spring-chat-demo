package com.example.chatdemo.service;

import java.util.Map;
import java.util.Optional;

/**
 * Interface for providers of hover card information.
 * Implementations can be registered as Spring beans to contribute
 * hover card data for different types of users.
 */
public interface HoverCardProvider {
    
    /**
     * Provides hover card information for a user.
     * If this provider can't handle the given user components, it should return an empty Optional.
     * 
     * @param providerType The provider type extracted from the ID (e.g., "oauth2")
     * @param providerName The provider name if available (e.g., "github")
     * @param username The username extracted from the ID (e.g., "username")
     * @param baseInfo Basic user info that can be enhanced
     * @return A map of key-value pairs with hover card information,
     *         or empty Optional if this provider can't handle these components
     */
    Optional<Map<String, String>> provideHoverCardInfo(
            String providerType,
            String providerName,
            String username,
            UserInfoService.UserInfo baseInfo);
}