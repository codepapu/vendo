package com.nexus.common.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {
    
    private final JavaMailSender mailSender;
    
    public void send(String to, String subject, String template, Map<String, Object> data) {
        try {
            // For now, simple email. Can be enhanced with Thymeleaf templates
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(buildMessage(template, data));
            
            mailSender.send(message);
            log.info("Email notification sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email notification to: {}", to, e);
            throw new RuntimeException("Failed to send email notification", e);
        }
    }
    
    private String buildMessage(String template, Map<String, Object> data) {
        // Simple template replacement
        // Can be enhanced with Thymeleaf or other templating engines
        String message = template;
        if (data != null) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                message = message.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
            }
        }
        return message;
    }
}
