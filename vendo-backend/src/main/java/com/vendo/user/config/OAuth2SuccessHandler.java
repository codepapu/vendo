package com.vendo.user.config;

import com.vendo.user.dto.response.AuthResponse;
import com.vendo.user.entity.User;
import com.vendo.user.repository.UserRepository;
import com.vendo.user.service.JwtService;
import com.vendo.user.service.SocialAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * OAuth2 success handler that redirects to frontend with JWT tokens
 * after successful OAuth2 authentication.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private final JwtService jwtService;
    private final UserRepository userRepository;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException {
        String targetUrl = determineTargetUrl(request, response, authentication);
        
        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect to {}", targetUrl);
            return;
        }
        
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
    
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) {
        try {
            if (authentication instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
                OAuth2User oauth2User = oauth2Token.getPrincipal();
                
                // Extract email from OAuth2 user
                String email = extractEmail(oauth2User, oauth2Token.getAuthorizedClientRegistrationId());
                
                if (email != null && !email.isEmpty()) {
                    // Find or create user
                    User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new RuntimeException("User not found after OAuth2 authentication"));
                    
                    // Generate JWT tokens
                    String accessToken = jwtService.generateToken(user);
                    String refreshToken = jwtService.generateRefreshToken(user);
                    
                    // Build AuthResponse
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
                    
                    // Encode tokens in URL (for frontend to extract)
                    return UriComponentsBuilder.fromUriString(frontendUrl + "/auth/oauth/callback")
                            .queryParam("access_token", accessToken)
                            .queryParam("refresh_token", refreshToken)
                            .queryParam("token_type", "Bearer")
                            .build().toUriString();
                }
            }
        } catch (Exception e) {
            log.error("Error generating OAuth2 success URL", e);
            // Redirect to error page
            return UriComponentsBuilder.fromUriString(frontendUrl + "/auth/oauth/error")
                    .queryParam("error", "authentication_failed")
                    .build().toUriString();
        }
        
        // Default redirect
        return frontendUrl + "/auth/oauth/success";
    }
    
    private String extractEmail(OAuth2User oauth2User, String registrationId) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        return switch (registrationId.toLowerCase()) {
            case "google" -> (String) attributes.get("email");
            case "facebook" -> {
                String email = (String) attributes.get("email");
                if (email == null) {
                    // Facebook might return email in different format
                    Object emailObj = attributes.get("email");
                    email = emailObj != null ? emailObj.toString() : null;
                }
                yield email;
            }
            case "github" -> {
                // GitHub email might be in a different attribute or require additional API call
                String email = (String) attributes.get("email");
                if (email == null) {
                    // Try login as fallback (GitHub username)
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
