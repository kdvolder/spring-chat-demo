package com.example.chatdemo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import com.example.chatdemo.service.UserInfoService;
import com.example.chatdemo.service.UserInfoService.UserInfo;
import com.example.chatdemo.service.UserInfoService.UserHoverCardInfo;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserInfoService userInfoService;
    
    public UserController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    /**
     * Get information about the currently authenticated user
     */
    @GetMapping("/user")
    public UserInfo getCurrentUser(Authentication authentication) {
        // Use the service to get complete user info including ID, display name, and roles
        return userInfoService.getCurrentUserInfo(authentication);
    }
    
    /**
     * Get complete information about the current user (private endpoint)
     * 
     * This endpoint is only for getting your own user information with all details.
     * For getting information about other users, use the /api/users/{userId}/public endpoint.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<UserInfo> getUserById(@PathVariable String userId, Authentication authentication) {
        // Only allow access to your own user info
        String currentUserId = userInfoService.getCurrentUserInfo(authentication).id();
        if (!currentUserId.equals(userId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        UserInfo userInfo = userInfoService.getUserInfoById(userId);
        if (userInfo == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(userInfo);
    }
    
    /**
     * Get public information about any user (public endpoint)
     * 
     * This endpoint is available to any authenticated user and only returns
     * non-sensitive information suitable for display in the chat UI.
     */
    @GetMapping("/users/{userId}/public")
    public ResponseEntity<UserInfoService.PublicUserInfo> getPublicUserInfo(@PathVariable String userId) {
        UserInfoService.PublicUserInfo publicInfo = userInfoService.getPublicUserInfoById(userId);
        
        if (publicInfo == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(publicInfo);
    }
    
    /**
     * Get hover card information for any user.
     * This endpoint returns additional details suitable for displaying in a hover card UI.
     * 
     * @param userId The user ID to get hover card info for
     * @return Hover card information with user details
     */
    @GetMapping("/users/{userId}/hovercard")
    public ResponseEntity<UserHoverCardInfo> getUserHoverCard(@PathVariable String userId) {
        System.out.println("🔍 Hover card requested for user: " + userId);
        
        UserHoverCardInfo hoverCardInfo = userInfoService.getHoverCardInfoById(userId);
        if (hoverCardInfo == null) {
            System.out.println("❌ User not found: " + userId);
            return ResponseEntity.notFound().build();
        }
        
        System.out.println("✅ Returning hover card info for: " + hoverCardInfo.displayName());
        return ResponseEntity.ok(hoverCardInfo);
    }
    
    /**
     * Get GitHub-specific hover card information for the current user.
     * This endpoint demonstrates using the OAuth2 token from the deprecated client
     * to make API calls to GitHub.
     * 
     * @param authentication The current user's authentication
     * @return Hover card information with GitHub-specific details
     */
    @GetMapping("/user/github-info")
    public ResponseEntity<UserHoverCardInfo> getGitHubInfo(Authentication authentication) {
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2Auth) || 
            !"github".equals(oauth2Auth.getAuthorizedClientRegistrationId())) {
            return ResponseEntity.badRequest().build();
        }
        
        UserInfo userInfo = userInfoService.getCurrentUserInfo(authentication);
        UserHoverCardInfo hoverCardInfo = userInfoService.getHoverCardInfoById(userInfo.id());
        
        if (hoverCardInfo == null) {
            return ResponseEntity.notFound().build();
        }
        
        System.out.println("✅ Returning GitHub info for: " + hoverCardInfo.displayName());
        return ResponseEntity.ok(hoverCardInfo);
    }
} 