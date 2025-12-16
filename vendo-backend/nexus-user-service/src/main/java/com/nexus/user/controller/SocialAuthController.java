package com.nexus.user.controller;

import com.nexus.common.dto.ApiResponse;
import com.nexus.user.service.SocialAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import java.util.Map;

/**
 * Controller for social login (OAuth2) endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth/oauth")
@RequiredArgsConstructor
public class SocialAuthController {
    
    private final SocialAuthService socialAuthService;
    
    /**
     * OAuth2 callback endpoint - handled by Spring Security OAuth2
     * This endpoint is called after successful OAuth2 authentication
     */
    @GetMapping("/success")
    public ResponseEntity<ApiResponse<String>> oauthSuccess(
            @AuthenticationPrincipal OAuth2User oauth2User) {
        // OAuth2 authentication is handled by Spring Security
        // After successful authentication, user is redirected here
        // In production, would generate JWT token and return it
        return ResponseEntity.ok(ApiResponse.success("OAuth2 authentication successful. Please configure OAuth2 client credentials in application.yml"));
    }
    
    /**
     * Get OAuth2 login URLs for different providers
     */
    @GetMapping("/providers")
    public ResponseEntity<ApiResponse<Map<String, String>>> getOAuthProviders() {
        // Return available OAuth2 providers
        Map<String, String> providers = Map.of(
                "google", "/oauth2/authorization/google",
                "facebook", "/oauth2/authorization/facebook",
                "github", "/oauth2/authorization/github"
        );
        return ResponseEntity.ok(ApiResponse.success(providers));
    }
}
