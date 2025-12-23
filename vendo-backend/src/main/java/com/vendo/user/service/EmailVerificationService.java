package com.vendo.user.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.user.entity.EmailVerificationToken;
import com.vendo.user.entity.User;
import com.vendo.user.repository.EmailVerificationTokenRepository;
import com.vendo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    
    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    
    private static final int TOKEN_EXPIRATION_HOURS = 24;
    
    @Transactional
    public void sendVerificationEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        if (user.getEmailVerifiedAt() != null) {
            throw new BadRequestException("Email is already verified");
        }
        
        // Delete existing unverified tokens
        tokenRepository.findByUserIdAndVerifiedAtIsNull(userId)
                .ifPresent(token -> tokenRepository.deleteById(token.getId()));
        
        // Generate new token
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS))
                .createdAt(LocalDateTime.now())
                .build();
        
        tokenRepository.save(verificationToken);
        
        // Send email
        emailService.sendVerificationEmail(user.getEmail(), token);
    }
    
    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid verification token"));
        
        if (verificationToken.isExpired()) {
            tokenRepository.delete(verificationToken);
            throw new BadRequestException("Verification token has expired");
        }
        
        if (verificationToken.isVerified()) {
            throw new BadRequestException("Email is already verified");
        }
        
        // Verify the email
        User user = verificationToken.getUser();
        
        LocalDateTime now = LocalDateTime.now();
        user.setEmailVerifiedAt(now);
        user.setIsVerified(true);
        userRepository.save(user);
        
        verificationToken.setVerifiedAt(now);
        tokenRepository.save(verificationToken);
        
        log.info("Email verified for user: {}", user.getEmail());
    }
    
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        
        if (user.getEmailVerifiedAt() != null) {
            throw new BadRequestException("Email is already verified");
        }
        
        sendVerificationEmail(user.getId());
    }
}
