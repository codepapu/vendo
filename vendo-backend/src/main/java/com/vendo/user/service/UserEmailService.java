package com.vendo.user.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * User Email Service
 * Wrapper service that uses the common email template service
 * Maintains backward compatibility with existing code
 */
@Slf4j
@Service("userEmailService")
@RequiredArgsConstructor
public class UserEmailService {
    
    private final JavaMailSender mailSender;
    private final com.vendo.common.email.service.EmailService commonEmailService;
    
    @Value("${spring.mail.from:noreply@vendo.com}")
    private String fromEmail;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    /**
     * Send password reset email using template
     */
    public void sendPasswordResetEmail(String email, String token) {
        try {
            commonEmailService.sendPasswordResetEmail(email, token);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", email, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    /**
     * Send email verification email using template
     */
    public void sendVerificationEmail(String email, String token) {
        try {
            commonEmailService.sendVerificationEmail(email, token);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", email, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    public void sendActivationEmail(String email, String activationCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Activate Your Account");
            message.setText("Your activation code is: " + activationCode + "\n\n" +
                    "Please use this code to activate your account.\n\n" +
                    "This code will expire in 24 hours.");
            
            mailSender.send(message);
            log.info("Activation email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send activation email to: {}", email, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    public void sendWelcomeEmail(String email, String name) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Welcome to Vendo!");
            message.setText("Hello " + name + ",\n\n" +
                    "Welcome to Vendo E-commerce Marketplace!\n\n" +
                    "Thank you for joining us. We're excited to have you on board.\n\n" +
                    "Happy shopping!");
            
            mailSender.send(message);
            log.info("Welcome email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", email, e);
            // Don't throw exception for welcome email
        }
    }
    
    /**
     * Send order confirmation email using template
     * @deprecated Use CommonEmailService.sendOrderConfirmationEmail instead
     */
    @Deprecated
    public void sendOrderConfirmationEmail(String email, String subject, String message, Object orderDetails) {
        try {
            // Fallback to simple email for backward compatibility
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(email);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);
            
            mailSender.send(mailMessage);
            log.info("Order confirmation email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send order confirmation email to: {}", email, e);
            // Don't throw exception - order is still created
        }
    }
}
