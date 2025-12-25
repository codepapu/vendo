package com.vendo.common.email.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Email Template Service
 * Handles email sending with Thymeleaf templates
 * Supports both HTML and plain text formats
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTemplateService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${spring.mail.from:noreply@vendo.com}")
    private String defaultFromEmail;
    
    @Value("${app.name:Vendo}")
    private String appName;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    /**
     * Send email with HTML template
     */
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name()
            );
            
            // Set email properties
            helper.setFrom(defaultFromEmail, appName);
            helper.setTo(to);
            helper.setSubject(subject);
            
            // Prepare context with variables
            Context context = new Context();
            context.setVariables(variables);
            context.setVariable("appName", appName);
            context.setVariable("frontendUrl", frontendUrl);
            context.setVariable("year", java.time.Year.now().getValue());
            
            // Process HTML template
            String htmlContent = templateEngine.process("emails/html/" + templateName, context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("HTML email sent successfully to: {} with template: {}", to, templateName);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {} with template: {}", to, templateName, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    /**
     * Send email with both HTML and plain text templates
     */
    public void sendMultipartEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name()
            );
            
            // Set email properties
            helper.setFrom(defaultFromEmail, appName);
            helper.setTo(to);
            helper.setSubject(subject);
            
            // Prepare context with variables
            Context context = new Context();
            context.setVariables(variables);
            context.setVariable("appName", appName);
            context.setVariable("frontendUrl", frontendUrl);
            context.setVariable("year", java.time.Year.now().getValue());
            
            // Process HTML template
            String htmlContent = templateEngine.process("emails/html/" + templateName, context);
            
            // Process plain text template
            String textContent = templateEngine.process("emails/text/" + templateName, context);
            
            helper.setText(textContent, htmlContent);
            
            mailSender.send(message);
            log.info("Multipart email sent successfully to: {} with template: {}", to, templateName);
        } catch (MessagingException e) {
            log.error("Failed to send multipart email to: {} with template: {}", to, templateName, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    /**
     * Send plain text email
     */
    public void sendTextEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                message,
                false,
                StandardCharsets.UTF_8.name()
            );
            
            // Set email properties
            helper.setFrom(defaultFromEmail, appName);
            helper.setTo(to);
            helper.setSubject(subject);
            
            // Prepare context with variables
            Context context = new Context();
            context.setVariables(variables);
            context.setVariable("appName", appName);
            context.setVariable("frontendUrl", frontendUrl);
            context.setVariable("year", java.time.Year.now().getValue());
            
            // Process plain text template
            String textContent = templateEngine.process("emails/text/" + templateName, context);
            helper.setText(textContent, false);
            
            mailSender.send(message);
            log.info("Text email sent successfully to: {} with template: {}", to, templateName);
        } catch (MessagingException e) {
            log.error("Failed to send text email to: {} with template: {}", to, templateName, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}

