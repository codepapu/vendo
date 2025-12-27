package com.vendo.notification.service;

import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.common.notification.PushNotificationService;
import com.vendo.common.sms.SmsService;
import com.vendo.notification.controller.NotificationWebSocketController;
import com.vendo.notification.dto.request.SendNotificationRequest;
import com.vendo.notification.dto.response.NotificationResponse;
import com.vendo.notification.entity.Notification;
import com.vendo.notification.enums.NotificationType;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceService preferenceService;
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
        NotificationType notificationType = request.getNotificationType();
        Long userId = request.getUserId();
        
        // Determine which channels to send via
        List<String> channelsToSend = determineChannelsToSend(request, notificationType, userId);
        
        if (channelsToSend.isEmpty()) {
            log.info("No enabled channels for notification type {} for user {}", notificationType, userId);
            return null;
        }
        
        // Create notification record (always save in-app notification)
        Notification notification = Notification.builder()
                .userId(userId)
                .type("in_app") // Store as in-app in database
                .title(request.getTitle())
                .message(request.getMessage())
                .data(convertDataToString(request.getData()))
                .isRead(false)
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        NotificationResponse notificationResponse = mapToResponse(savedNotification);
        
        // Send via all enabled channels
        for (String channel : channelsToSend) {
            sendViaChannel(request, channel);
        }
        
        // Send real-time notification via WebSocket if in-app is enabled
        if (channelsToSend.contains("in_app") || channelsToSend.contains("in-app")) {
            try {
                webSocketController.sendNotificationToUser(userId, notificationResponse);
            } catch (Exception e) {
                log.warn("Failed to send WebSocket notification", e);
            }
        }
        
        log.info("Notification sent: ID={}, Type={}, Channels={}, UserID={}", 
                savedNotification.getId(), notificationType, channelsToSend, userId);
        
        return notificationResponse;
    }
    
    /**
     * Determine which channels to send the notification via based on user preferences.
     */
    private List<String> determineChannelsToSend(SendNotificationRequest request, 
                                                  NotificationType notificationType, 
                                                  Long userId) {
        List<String> channelsToSend = new ArrayList<>();
        
        // If specific channels are requested, use those (but still check preferences)
        if (request.getChannels() != null && !request.getChannels().isEmpty()) {
            for (String channel : request.getChannels()) {
                if (notificationType != null) {
                    if (preferenceService.isChannelEnabledForType(userId, notificationType, channel)) {
                        channelsToSend.add(channel);
                    }
                } else {
                    // No notification type specified, allow all requested channels
                    channelsToSend.add(channel);
                }
            }
            return channelsToSend;
        }
        
        // If single channel is specified, use that (but check preferences)
        if (request.getChannel() != null && !request.getChannel().isEmpty()) {
            String channel = request.getChannel();
            if (notificationType != null) {
                if (preferenceService.isChannelEnabledForType(userId, notificationType, channel)) {
                    channelsToSend.add(channel);
                }
            } else {
                channelsToSend.add(channel);
            }
            return channelsToSend;
        }
        
        // No channels specified - use user preferences to determine channels
        if (notificationType != null) {
            // Check each channel based on notification type preferences
            if (preferenceService.isChannelEnabledForType(userId, notificationType, "email")) {
                channelsToSend.add("email");
            }
            if (preferenceService.isChannelEnabledForType(userId, notificationType, "sms")) {
                channelsToSend.add("sms");
            }
            if (preferenceService.isChannelEnabledForType(userId, notificationType, "push")) {
                channelsToSend.add("push");
            }
            if (preferenceService.isChannelEnabledForType(userId, notificationType, "in_app")) {
                channelsToSend.add("in_app");
            }
        } else {
            // No notification type - send via all channels (default behavior)
            channelsToSend.add("email");
            channelsToSend.add("sms");
            channelsToSend.add("push");
            channelsToSend.add("in_app");
        }
        
        return channelsToSend;
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
    
    private void sendViaChannel(SendNotificationRequest request, String channel) {
        switch (channel.toLowerCase()) {
            case "email" -> sendEmail(request);
            case "sms" -> sendSMS(request);
            case "push" -> sendPush(request);
            case "in_app", "in-app" -> // Already saved to database
                    log.debug("In-app notification saved");
            default -> log.warn("Unknown notification channel: {}", channel);
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
    
}
