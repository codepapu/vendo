package com.vendo.user.service;

import com.vendo.common.event.EventPublisher;
import com.vendo.common.event.user.UserCreatedEvent;
import com.vendo.common.exception.BadRequestException;
import com.vendo.user.dto.response.AuthResponse;
import com.vendo.user.entity.User;
import com.vendo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Service for handling social login (OAuth2) authentication.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SocialAuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EventPublisher eventPublisher;
    private final DefaultOAuth2UserService oauth2UserService = new DefaultOAuth2UserService();
    
    /**
     * Process OAuth2 user and create/login user.
     */
    @Transactional
    public AuthResponse processOAuth2User(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = oauth2UserService.loadUser(oAuth2UserRequest);
        
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        String email = extractEmail(oAuth2User, registrationId);
        String name = extractName(oAuth2User, registrationId);
        
        if (email == null || email.isEmpty()) {
            throw new BadRequestException("Email not found in OAuth2 user information");
        }
        
        // Check if user exists
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) {
            // Create new user
            user = createUserFromOAuth2(email, name, registrationId);
            
            // Publish UserCreatedEvent
            UserCreatedEvent userCreatedEvent = new UserCreatedEvent(
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    "USER"
            );
            eventPublisher.publish(userCreatedEvent, "user-created");
        } else {
            // Update last login
            user.setLastLogin(LocalDateTime.now());
            user = userRepository.save(user);
        }
        
        // Generate JWT tokens
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        
        return AuthResponse.builder()
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
                        .build())
                .build();
    }
    
    private User createUserFromOAuth2(String email, String name, String provider) {
        String[] nameParts = name != null ? name.split(" ", 2) : new String[]{"", ""};
        String firstName = nameParts.length > 0 ? nameParts[0] : "";
        String lastName = nameParts.length > 1 ? nameParts[1] : "";
        
        // Generate random password for OAuth users
        String randomPassword = UUID.randomUUID().toString();
        
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(randomPassword))
                .firstName(firstName)
                .lastName(lastName)
                .username(email.split("@")[0] + "_" + provider)
                .superUser(false)
                .manageSupers(false)
                .build();
        
        return userRepository.save(user);
    }
    
    private String extractEmail(OAuth2User oAuth2User, String registrationId) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        return switch (registrationId.toLowerCase()) {
            case "google" -> (String) attributes.get("email");
            case "facebook" -> (String) attributes.get("email");
            case "github" -> (String) attributes.get("email");
            default -> (String) attributes.get("email");
        };
    }
    
    private String extractName(OAuth2User oAuth2User, String registrationId) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        return switch (registrationId.toLowerCase()) {
            case "google" -> (String) attributes.get("name");
            case "facebook" -> (String) attributes.get("name");
            case "github" -> (String) attributes.get("name");
            default -> (String) attributes.get("name");
        };
    }
}
