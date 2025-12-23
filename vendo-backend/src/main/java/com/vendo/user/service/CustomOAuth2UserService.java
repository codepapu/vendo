package com.vendo.user.service;

import com.vendo.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * Custom OAuth2UserService that integrates with SocialAuthService
 * to process OAuth2 users and create/login users in the system.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private final SocialAuthService socialAuthService;
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // First, load the OAuth2 user using the default service
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        try {
            // Process the OAuth2 user and create/login user in our system
            // This will be called by Spring Security during OAuth2 authentication flow
            socialAuthService.processOAuth2User(userRequest);
            log.info("OAuth2 user processed successfully for provider: {}", 
                    userRequest.getClientRegistration().getRegistrationId());
        } catch (Exception e) {
            log.error("Error processing OAuth2 user", e);
            throw new OAuth2AuthenticationException("Failed to process OAuth2 user", e);
        }
        
        return oauth2User;
    }
}
