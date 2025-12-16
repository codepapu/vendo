package com.nexus.user.service;

import com.nexus.common.exception.BadRequestException;
import com.nexus.common.exception.ResourceNotFoundException;
import com.nexus.user.entity.Activation;
import com.nexus.user.entity.User;
import com.nexus.user.repository.ActivationRepository;
import com.nexus.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivationService {
    
    private final UserRepository userRepository;
    private final ActivationRepository activationRepository;
    private final EmailService emailService;
    private final Random random = new Random();
    
    @Transactional
    public void createActivation(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Check if already activated
        if (activationRepository.existsByUserIdAndCompletedTrue(userId)) {
            throw new BadRequestException("User is already activated");
        }
        
        // Delete existing incomplete activations
        activationRepository.findByUserIdAndCompletedFalse(userId)
                .ifPresent(activationRepository::delete);
        
        // Generate activation code (6 digits)
        String code = String.format("%06d", random.nextInt(1000000));
        
        Activation activation = Activation.builder()
                .user(user)
                .code(code)
                .completed(false)
                .build();
        
        activation = activationRepository.save(activation);
        
        // Send activation email
        emailService.sendActivationEmail(user.getEmail(), code);
        
        log.info("Activation code generated for user: {}", user.getEmail());
    }
    
    @Transactional
    public void activateUser(String code) {
        Activation activation = activationRepository.findByCodeAndCompletedFalse(code)
                .orElseThrow(() -> new BadRequestException("Invalid or expired activation code"));
        
        User user = activation.getUser();
        
        // Mark activation as completed
        activation.setCompleted(true);
        activation.setCompletedAt(LocalDateTime.now());
        activationRepository.save(activation);
        
        log.info("User activated: {}", user.getEmail());
    }
    
    @Transactional
    public void resendActivationCode(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        if (activationRepository.existsByUserIdAndCompletedTrue(userId)) {
            throw new BadRequestException("User is already activated");
        }
        
        createActivation(userId);
    }
    
    public boolean isUserActivated(Long userId) {
        return activationRepository.existsByUserIdAndCompletedTrue(userId);
    }
}
