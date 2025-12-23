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
            // Link social account to existing user
            // Update last login
            user.setLastLogin(LocalDateTime.now());
            
            // If email is not verified, mark as verified for OAuth users
            if (!user.getIsVerified() && user.getEmailVerifiedAt() == null) {
                user.setIsVerified(true);
                user.setEmailVerifiedAt(LocalDateTime.now());
            }
            
            user = userRepository.save(user);
            log.info("Linked social account {} to existing user: {}", registrationId, email);
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
                        .isVerified(user.getIsVerified())
                        .emailVerifiedAt(user.getEmailVerifiedAt())
                        .isActive(user.getIsActive())
                        .userType(user.getUserType())
                        .build())
                .build();
    }
    
    private User createUserFromOAuth2(String email, String name, String provider) {
        String[] nameParts = name != null ? name.split(" ", 2) : new String[]{"", ""};
        String firstName = nameParts.length > 0 ? nameParts[0] : "";
        String lastName = nameParts.length > 1 ? nameParts[1] : "";
        
        // Generate random password for OAuth users
        String randomPassword = UUID.randomUUID().toString();
        
        // Generate unique username
        String baseUsername = email.split("@")[0] + "_" + provider;
        String username = baseUsername;
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(randomPassword))
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .userType("CUSTOMER")
                .isActive(true)
                .isVerified(true) // OAuth users are considered verified
                .emailVerifiedAt(LocalDateTime.now())
                .superUser(false)
                .manageSupers(false)
                .build();
        
        return userRepository.save(user);
    }
    
    private String extractEmail(OAuth2User oAuth2User, String registrationId) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        return switch (registrationId.toLowerCase()) {
            case "google" -> (String) attributes.get("email");
            case "facebook" -> {
                // Facebook email might be null if user hasn't granted email permission
                String email = (String) attributes.get("email");
                if (email == null) {
                    // Try to get from id (not ideal, but fallback)
                    String id = (String) attributes.get("id");
                    if (id != null) {
                        email = id + "@facebook.com";
                    }
                }
                yield email;
            }
            case "github" -> {
                // GitHub email might be null or require additional API call
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
    
    private String extractName(OAuth2User oAuth2User, String registrationId) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        return switch (registrationId.toLowerCase()) {
            case "google" -> (String) attributes.get("name");
            case "facebook" -> (String) attributes.get("name");
            case "github" -> {
                // GitHub uses 'name' or 'login' as fallback
                String name = (String) attributes.get("name");
                if (name == null || name.isEmpty()) {
                    name = (String) attributes.get("login");
                }
                yield name;
            }
            default -> (String) attributes.get("name");
        };
    }
    
    /**
     * Link social account to existing user.
     * This method allows linking a social account to an already registered user.
     */
    @Transactional
    public AuthResponse linkSocialAccount(String email, String provider, OAuth2User oAuth2User) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found with email: " + email));
        
        // Update last login
        user.setLastLogin(LocalDateTime.now());
        
        // If email is not verified, mark as verified for OAuth users
        if (!user.getIsVerified() && user.getEmailVerifiedAt() == null) {
            user.setIsVerified(true);
            user.setEmailVerifiedAt(LocalDateTime.now());
        }
        
        user = userRepository.save(user);
        
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
                        .isVerified(user.getIsVerified())
                        .emailVerifiedAt(user.getEmailVerifiedAt())
                        .isActive(user.getIsActive())
                        .userType(user.getUserType())
                        .build())
                .build();
    }
}
