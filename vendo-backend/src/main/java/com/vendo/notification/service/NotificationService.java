package com.vendo.notification.service;

import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.common.notification.PushNotificationService;
import com.vendo.common.sms.SmsService;
import com.vendo.notification.controller.NotificationWebSocketController;
import com.vendo.notification.dto.request.NotificationPreferenceRequest;
import com.vendo.notification.dto.request.SendNotificationRequest;
import com.vendo.notification.dto.response.NotificationPreferenceResponse;
import com.vendo.notification.dto.response.NotificationResponse;
import com.vendo.notification.entity.Notification;
import com.vendo.notification.entity.NotificationPreference;
import com.vendo.notification.repository.NotificationPreferenceRepository;
import com.vendo.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final JavaMailSender mailSender;
    private final SmsService smsService;
    private final PushNotificationService pushNotificationService;
    private final NotificationWebSocketController webSocketController;
    
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
    
    @Transactional
    public NotificationResponse sendNotification(SendNotificationRequest request) {
        // Check user preferences
        NotificationPreference preference = preferenceRepository.findByUserId(request.getUserId())
                .orElse(null);
        
        // Check if notification type is enabled
        if (preference != null && !isNotificationTypeEnabled(preference, request.getType())) {
            log.info("Notification type {} is disabled for user {}", request.getType(), request.getUserId());
            return null;
        }
        
        // Create notification record
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .title(request.getTitle())
                .message(request.getMessage())
                .data(convertDataToString(request.getData()))
                .isRead(false)
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        NotificationResponse notificationResponse = mapToResponse(savedNotification);
        
        // Send via appropriate channel
        sendViaChannel(request);
        
        // Send real-time notification via WebSocket if in-app
        if ("in_app".equals(request.getType())) {
            try {
                webSocketController.sendNotificationToUser(request.getUserId(), notificationResponse);
            } catch (Exception e) {
                log.warn("Failed to send WebSocket notification", e);
            }
        }
        
        log.info("Notification sent: ID={}, Type={}, UserID={}", 
                savedNotification.getId(), request.getType(), request.getUserId());
        
        return notificationResponse;
    }
    
    @Transactional
    public NotificationResponse markAsRead(Long id, Long userId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        
        if (!notification.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Notification", "id", id);
        }
        
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        Notification updatedNotification = notificationRepository.save(notification);
        
        return mapToResponse(updatedNotification);
    }
    
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalse(userId);
        notifications.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
        });
        notificationRepository.saveAll(notifications);
    }
    
    @Transactional(readOnly = true)
    public NotificationPreferenceResponse getUserPreferences(Long userId) {
        NotificationPreference preference = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // Create default preferences if not exists
                    NotificationPreference defaultPref = NotificationPreference.builder()
                            .userId(userId)
                            .emailEnabled(true)
                            .smsEnabled(false)
                            .pushEnabled(true)
                            .inAppEnabled(true)
                            .build();
                    return preferenceRepository.save(defaultPref);
                });
        return mapPreferenceToResponse(preference);
    }
    
    @Transactional
    public NotificationPreferenceResponse updateUserPreferences(Long userId, NotificationPreferenceRequest request) {
        NotificationPreference preference = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> NotificationPreference.builder()
                        .userId(userId)
                        .emailEnabled(true)
                        .smsEnabled(false)
                        .pushEnabled(true)
                        .inAppEnabled(true)
                        .build());
        
        if (request.getEmailEnabled() != null) {
            preference.setEmailEnabled(request.getEmailEnabled());
        }
        if (request.getSmsEnabled() != null) {
            preference.setSmsEnabled(request.getSmsEnabled());
        }
        if (request.getPushEnabled() != null) {
            preference.setPushEnabled(request.getPushEnabled());
        }
        if (request.getInAppEnabled() != null) {
            preference.setInAppEnabled(request.getInAppEnabled());
        }
        
        NotificationPreference savedPreference = preferenceRepository.save(preference);
        log.info("Notification preferences updated for user: {}", userId);
        
        return mapPreferenceToResponse(savedPreference);
    }
    
    private boolean isNotificationTypeEnabled(NotificationPreference preference, String type) {
        return switch (type) {
            case "email" -> preference.getEmailEnabled();
            case "sms" -> preference.getSmsEnabled();
            case "push" -> preference.getPushEnabled();
            case "in_app" -> preference.getInAppEnabled();
            default -> true;
        };
    }
    
    private void sendViaChannel(SendNotificationRequest request) {
        switch (request.getType()) {
            case "email" -> sendEmail(request);
            case "sms" -> sendSMS(request);
            case "push" -> sendPush(request);
            case "in_app" -> // Already saved to database
                    log.debug("In-app notification saved");
            default -> log.warn("Unknown notification type: {}", request.getType());
        }
    }
    
    private void sendEmail(SendNotificationRequest request) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("user@example.com"); // Would get from user service
            message.setSubject(request.getTitle());
            message.setText(request.getMessage());
            mailSender.send(message);
            log.info("Email notification sent to user {}", request.getUserId());
        } catch (Exception e) {
            log.error("Failed to send email notification", e);
        }
    }
    
    private void sendSMS(SendNotificationRequest request) {
        try {
            // Get user phone number from user service (for now, use placeholder)
            // In production, would fetch from user service
            String phoneNumber = "+1234567890"; // Placeholder - would get from user service
            
            smsService.sendSms(phoneNumber, request.getMessage());
            log.info("SMS notification sent to user {}", request.getUserId());
        } catch (Exception e) {
            log.error("Failed to send SMS notification to user {}", request.getUserId(), e);
        }
    }
    
    private void sendPush(SendNotificationRequest request) {
        try {
            // Get user device token from user service (for now, use placeholder)
            // In production, would fetch device tokens from user service
            String deviceToken = "device-token-placeholder"; // Placeholder - would get from user service
            
            Map<String, Object> pushData = new HashMap<>();
            if (request.getData() != null) {
                pushData.putAll(request.getData());
            }
            pushData.put("notificationId", request.getUserId());
            
            pushNotificationService.send(deviceToken, request.getTitle(), request.getMessage(), pushData);
            log.info("Push notification sent to user {}", request.getUserId());
        } catch (Exception e) {
            log.error("Failed to send push notification to user {}", request.getUserId(), e);
        }
    }
    
    private String convertDataToString(java.util.Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.warn("Failed to serialize notification data to JSON", e);
            return data.toString();
        }
    }
    
    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .data(notification.getData())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
    
    private NotificationPreferenceResponse mapPreferenceToResponse(NotificationPreference preference) {
        return NotificationPreferenceResponse.builder()
                .id(preference.getId())
                .userId(preference.getUserId())
                .emailEnabled(preference.getEmailEnabled())
                .smsEnabled(preference.getSmsEnabled())
                .pushEnabled(preference.getPushEnabled())
                .inAppEnabled(preference.getInAppEnabled())
                .createdAt(preference.getCreatedAt())
                .updatedAt(preference.getUpdatedAt())
                .build();
    }
}
