package com.vendo.user.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.user.dto.response.AuthResponse;
import com.vendo.user.entity.User;
import com.vendo.user.repository.UserRepository;
import com.vendo.user.service.JwtService;
import com.vendo.user.service.SocialAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Controller for social login (OAuth2) endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth/oauth")
@RequiredArgsConstructor
public class SocialAuthController {
    
    private final SocialAuthService socialAuthService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    
    /**
     * OAuth2 callback endpoint - called after successful OAuth2 authentication
     * This endpoint is used as a fallback if the success handler doesn't redirect properly
     */
    @GetMapping("/success")
    public ResponseEntity<ApiResponse<AuthResponse>> oauthSuccess(
            @AuthenticationPrincipal OAuth2User oauth2User,
            OAuth2AuthenticationToken token) {
        
        if (oauth2User == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("OAuth2 authentication failed"));
        }
        
        try {
            // Extract email from OAuth2 user
            Map<String, Object> attributes = oauth2User.getAttributes();
            String registrationId = token.getAuthorizedClientRegistrationId();
            String email = extractEmail(attributes, registrationId);
            
            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Email not found in OAuth2 user information"));
            }
            
            // Find user
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found after OAuth2 authentication"));
            
            // Generate JWT tokens
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            
            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresAt(LocalDateTime.now().plusHours(24))
                    .user(AuthResponse.UserDto.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .username(user.getUsername())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .superUser(user.getSuperUser())
                            .isVerified(user.getIsVerified())
                            .emailVerifiedAt(user.getEmailVerifiedAt())
                            .isActive(user.getIsActive())
                            .userType(user.getUserType())
                            .build())
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success("OAuth2 authentication successful", authResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to process OAuth2 authentication: " + e.getMessage()));
        }
    }
    
    /**
     * Get OAuth2 login URLs for different providers
     */
    @GetMapping("/providers")
    public ResponseEntity<ApiResponse<Map<String, String>>> getOAuthProviders() {
        // Return available OAuth2 providers with full URLs
        Map<String, String> providers = Map.of(
                "google", "/oauth2/authorization/google",
                "facebook", "/oauth2/authorization/facebook",
                "github", "/oauth2/authorization/github"
        );
        return ResponseEntity.ok(ApiResponse.success(providers));
    }
    
    private String extractEmail(Map<String, Object> attributes, String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> (String) attributes.get("email");
            case "facebook" -> {
                String email = (String) attributes.get("email");
                if (email == null) {
                    String id = (String) attributes.get("id");
                    if (id != null) {
                        email = id + "@facebook.com";
                    }
                }
                yield email;
            }
            case "github" -> {
                String email = (String) attributes.get("email");
                if (email == null) {
                    String login = (String) attributes.get("login");
                    if (login != null) {
                        email = login + "@users.noreply.github.com";
                    }
                }
                yield email;
            }
            default -> (String) attributes.get("email");
        };
    }
}
