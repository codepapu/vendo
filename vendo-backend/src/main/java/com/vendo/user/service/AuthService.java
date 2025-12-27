package com.vendo.user.service;

import com.vendo.common.event.EventPublisher;
import com.vendo.common.event.user.UserCreatedEvent;
import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.user.dto.request.ChangePasswordRequest;
import com.vendo.user.dto.request.LoginRequest;
import com.vendo.user.dto.request.RegisterRequest;
import com.vendo.user.dto.request.ResetPasswordRequest;
import com.vendo.user.dto.request.VerifyEmailRequest;
import com.vendo.user.dto.response.AuthResponse;
import com.vendo.user.entity.User;
import com.vendo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;
    private final TokenBlacklistService tokenBlacklistService;
    private final EventPublisher eventPublisher;
    private final TwoFactorAuthService twoFactorAuthService;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        
        // Check if username already exists (if provided)
        if (request.getUsername() != null && userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        
        // Generate username if not provided
        String username = request.getUsername();
        if (username == null || username.trim().isEmpty()) {
            // Generate username from email (before @)
            username = request.getEmail().split("@")[0];
            // Ensure uniqueness
            int counter = 1;
            String baseUsername = username;
            while (userRepository.existsByUsername(username)) {
                username = baseUsername + counter;
                counter++;
            }
        }
        
        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(username)
                .phone(request.getPhone())
                .userType("CUSTOMER")
                .isActive(true)
                .isVerified(false)
                .superUser(false)
                .manageSupers(false)
                .build();
        
        user = userRepository.save(user);
        
        // Send verification email
        try {
            emailVerificationService.sendVerificationEmail(user.getId());
        } catch (Exception e) {
            log.error("Failed to send verification email", e);
            // Continue even if email fails
        }
        
        // Publish UserCreatedEvent
        UserCreatedEvent userCreatedEvent = new UserCreatedEvent(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getSuperUser() ? "ADMIN" : "USER"
        );
        eventPublisher.publish(userCreatedEvent, "user-created");
        
        // Generate JWT token
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
    
    public AuthResponse login(LoginRequest request) {
        // Find user by email or username
        User user = userRepository.findByEmail(request.getEmailOrUsername())
                .orElseGet(() -> userRepository.findByUsername(request.getEmailOrUsername())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "emailOrUsername", request.getEmailOrUsername())));
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }
        
        // Check if user is active
        if (!user.getIsActive()) {
            throw new BadRequestException("Account is deactivated. Please contact support.");
        }
        
        // Check if email is verified (optional - can be made required)
        if (!user.getIsVerified() && user.getEmailVerifiedAt() == null) {
            throw new BadRequestException("Email is not verified. Please check your email for verification link.");
        }
        
        // Check if 2FA is enabled
        boolean is2FAEnabled = twoFactorAuthService.is2FAEnabled(user.getId());
        if (is2FAEnabled) {
            // If 2FA is enabled but no code provided, throw exception indicating 2FA is required
            if (request.getTwoFactorCode() == null || request.getTwoFactorCode().isEmpty()) {
                throw new BadRequestException("2FA code is required. Please provide the code from your authenticator app.");
            }
            
            // Verify 2FA code
            if (!twoFactorAuthService.verify2FACodeForLogin(user.getId(), request.getTwoFactorCode())) {
                throw new BadRequestException("Invalid 2FA code");
            }
        }
        
        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
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
    
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw new BadRequestException("Invalid refresh token");
        }
        
        String email = jwtService.extractUsernameFromRefreshToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        
        // Generate new access token
        String newAccessToken = jwtService.generateToken(user);
        
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // Keep same refresh token
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
    
    @Transactional
    public void logout(String accessToken, String refreshToken) {
        // Extract expiration time from tokens to set blacklist expiration
        try {
            // Blacklist access token
            if (accessToken != null && !accessToken.isEmpty()) {
                // Extract expiration from token (default to 24 hours if can't extract)
                long expirationHours = 24;
                try {
                    java.util.Date expiration = jwtService.extractExpiration(accessToken);
                    long expirationMillis = expiration.getTime() - System.currentTimeMillis();
                    expirationHours = Math.max(1, expirationMillis / (1000 * 60 * 60)); // Convert to hours
                } catch (Exception e) {
                    log.warn("Could not extract expiration from access token, using default", e);
                }
                tokenBlacklistService.blacklistToken(accessToken, expirationHours);
            }
            
            // Blacklist refresh token
            if (refreshToken != null && !refreshToken.isEmpty()) {
                // Refresh tokens typically last 7 days
                tokenBlacklistService.blacklistToken(refreshToken, 168); // 7 days in hours
            }
            
            log.info("User logged out successfully");
        } catch (Exception e) {
            log.error("Error during logout", e);
            throw new BadRequestException("Failed to logout");
        }
    }
    
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        emailVerificationService.verifyEmail(request.getToken());
        log.info("Email verified successfully");
    }
    
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        log.info("Password reset successfully for email: {}", request.getEmail());
    }
    
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        passwordResetService.changePassword(
            userId,
            request.getCurrentPassword(),
            request.getNewPassword()
        );
        log.info("Password changed successfully for user ID: {}", userId);
    }
    
    @Transactional
    public void changePassword(Authentication authentication, ChangePasswordRequest request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        
        changePassword(user.getId(), request);
    }
    
    @Transactional
    public void forgotPassword(String email) {
        com.vendo.user.dto.request.ForgotPasswordRequest request = new com.vendo.user.dto.request.ForgotPasswordRequest();
        request.setEmail(email);
        passwordResetService.forgotPassword(request);
        log.info("Password reset email sent to: {}", email);
    }
    
    @Transactional
    public void resendVerificationEmail(String email) {
        emailVerificationService.resendVerificationEmail(email);
        log.info("Verification email resent to: {}", email);
    }
}
