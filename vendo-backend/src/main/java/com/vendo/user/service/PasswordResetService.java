package com.vendo.user.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.user.dto.request.ForgotPasswordRequest;
import com.vendo.user.dto.request.ResetPasswordRequest;
import com.vendo.user.entity.PasswordResetToken;
import com.vendo.user.entity.User;
import com.vendo.user.repository.PasswordResetTokenRepository;
import com.vendo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {
    
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEmailService emailService;
    
    private static final int TOKEN_EXPIRATION_HOURS = 1;
    
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));
        
        // Generate reset token
        String token = UUID.randomUUID().toString();
        
        // Delete existing token if any
        tokenRepository.deleteByEmail(user.getEmail());
        
        // Save new token
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .email(user.getEmail())
                .token(token)
                .createdAt(LocalDateTime.now())
                .build();
        
        tokenRepository.save(resetToken);
        
        // Send email
        emailService.sendPasswordResetEmail(user.getEmail(), token);
        log.info("Password reset token generated for user: {}", user.getEmail());
    }
    
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // Find token
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));
        
        // Check if token is expired (1 hour)
        if (resetToken.getCreatedAt().plusHours(TOKEN_EXPIRATION_HOURS).isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new BadRequestException("Reset token has expired");
        }
        
        // Verify email matches
        if (!resetToken.getEmail().equals(request.getEmail())) {
            throw new BadRequestException("Email does not match reset token");
        }
        
        // Find user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        
        // Delete token
        tokenRepository.delete(resetToken);
        
        log.info("Password reset successful for user: {}", user.getEmail());
    }
    
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("Password changed successfully for user: {}", user.getEmail());
    }
}
