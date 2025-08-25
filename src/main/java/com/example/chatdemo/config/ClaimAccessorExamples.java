package com.example.chatdemo.config;

import org.springframework.security.oauth2.core.ClaimAccessor;
import java.util.Map;

public class ClaimAccessorExamples {

    // Method demonstrating the deprecated containsClaim method
    @SuppressWarnings("deprecation")
    public boolean usesDeprecatedContainsClaim(ClaimAccessor claims, String claimName) {
        return claims.containsClaim(claimName);
    }

    // Method demonstrating the replacement hasClaim method
    public boolean usesNewHasClaim(ClaimAccessor claims, String claimName) {
        return claims.hasClaim(claimName);
    }

    // Example of a dummy ClaimAccessor implementation for testing
    public static class MyClaimAccessor implements ClaimAccessor {
        private final Map<String, Object> claims;

        public MyClaimAccessor(Map<String, Object> claims) {
            this.claims = claims;
        }

        @Override
        public Map<String, Object> getClaims() {
            return this.claims;
        }
    }
}

