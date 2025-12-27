package com.vendo.notification.service;

import com.vendo.notification.dto.request.NotificationPreferenceRequest;
import com.vendo.notification.dto.request.NotificationTypePreferenceRequest;
import com.vendo.notification.dto.response.NotificationPreferenceResponse;
import com.vendo.notification.dto.response.NotificationTypePreferenceResponse;
import com.vendo.notification.entity.NotificationPreference;
import com.vendo.notification.entity.NotificationTypePreference;
import com.vendo.notification.enums.NotificationType;
import com.vendo.notification.repository.NotificationPreferenceRepository;
import com.vendo.notification.repository.NotificationTypePreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing notification preferences.
 * Supports both global channel preferences and granular preferences per notification type.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPreferenceService {
    
    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationTypePreferenceRepository typePreferenceRepository;
    
    /**
     * Get all notification preferences for a user.
     * Includes both global channel preferences and granular type-specific preferences.
     */
    @Transactional(readOnly = true)
    public NotificationPreferenceResponse getPreferences(Long userId) {
        // Get or create global preferences
        NotificationPreference globalPreference = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    NotificationPreference defaultPref = NotificationPreference.builder()
                            .userId(userId)
                            .emailEnabled(true)
                            .smsEnabled(false)
                            .pushEnabled(true)
                            .inAppEnabled(true)
                            .build();
                    return preferenceRepository.save(defaultPref);
                });
        
        // Get all type-specific preferences
        List<NotificationTypePreference> typePreferences = typePreferenceRepository.findByUserId(userId);
        
        return NotificationPreferenceResponse.builder()
                .id(globalPreference.getId())
                .userId(globalPreference.getUserId())
                .emailEnabled(globalPreference.getEmailEnabled())
                .smsEnabled(globalPreference.getSmsEnabled())
                .pushEnabled(globalPreference.getPushEnabled())
                .inAppEnabled(globalPreference.getInAppEnabled())
                .typePreferences(typePreferences.stream()
                        .map(this::mapTypePreferenceToResponse)
                        .collect(Collectors.toList()))
                .createdAt(globalPreference.getCreatedAt())
                .updatedAt(globalPreference.getUpdatedAt())
                .build();
    }
    
    /**
     * Update global notification channel preferences.
     */
    @Transactional
    public NotificationPreferenceResponse updatePreferences(Long userId, NotificationPreferenceRequest request) {
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
        log.info("Global notification preferences updated for user: {}", userId);
        
        // Return full preferences including type-specific ones
        return getPreferences(userId);
    }
    
    /**
     * Get preference for a specific notification type.
     * Returns type-specific preference if exists, otherwise returns global preference.
     */
    @Transactional(readOnly = true)
    public NotificationTypePreferenceResponse getPreferenceByType(Long userId, NotificationType notificationType) {
        // First check for type-specific preference
        NotificationTypePreference typePreference = typePreferenceRepository
                .findByUserIdAndNotificationType(userId, notificationType)
                .orElse(null);
        
        if (typePreference != null) {
            return mapTypePreferenceToResponse(typePreference);
        }
        
        // Fallback to global preferences
        NotificationPreference globalPreference = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    NotificationPreference defaultPref = NotificationPreference.builder()
                            .userId(userId)
                            .emailEnabled(true)
                            .smsEnabled(false)
                            .pushEnabled(true)
                            .inAppEnabled(true)
                            .build();
                    return preferenceRepository.save(defaultPref);
                });
        
        // Return global preferences as type preference response
        return NotificationTypePreferenceResponse.builder()
                .userId(globalPreference.getUserId())
                .notificationType(notificationType)
                .emailEnabled(globalPreference.getEmailEnabled())
                .smsEnabled(globalPreference.getSmsEnabled())
                .pushEnabled(globalPreference.getPushEnabled())
                .inAppEnabled(globalPreference.getInAppEnabled())
                .build();
    }
    
    /**
     * Update or create preference for a specific notification type.
     */
    @Transactional
    public NotificationTypePreferenceResponse updateTypePreference(Long userId, NotificationTypePreferenceRequest request) {
        if (request.getNotificationType() == null) {
            throw new IllegalArgumentException("Notification type is required");
        }
        
        NotificationTypePreference typePreference = typePreferenceRepository
                .findByUserIdAndNotificationType(userId, request.getNotificationType())
                .orElseGet(() -> {
                    // Get global preferences as defaults
                    NotificationPreference globalPref = preferenceRepository.findByUserId(userId)
                            .orElseGet(() -> NotificationPreference.builder()
                                    .userId(userId)
                                    .emailEnabled(true)
                                    .smsEnabled(false)
                                    .pushEnabled(true)
                                    .inAppEnabled(true)
                                    .build());
                    
                    return NotificationTypePreference.builder()
                            .userId(userId)
                            .notificationType(request.getNotificationType())
                            .emailEnabled(globalPref.getEmailEnabled())
                            .smsEnabled(globalPref.getSmsEnabled())
                            .pushEnabled(globalPref.getPushEnabled())
                            .inAppEnabled(globalPref.getInAppEnabled())
                            .build();
                });
        
        // Update only provided fields
        if (request.getEmailEnabled() != null) {
            typePreference.setEmailEnabled(request.getEmailEnabled());
        }
        if (request.getSmsEnabled() != null) {
            typePreference.setSmsEnabled(request.getSmsEnabled());
        }
        if (request.getPushEnabled() != null) {
            typePreference.setPushEnabled(request.getPushEnabled());
        }
        if (request.getInAppEnabled() != null) {
            typePreference.setInAppEnabled(request.getInAppEnabled());
        }
        
        NotificationTypePreference savedPreference = typePreferenceRepository.save(typePreference);
        log.info("Notification type preference updated for user: {}, type: {}", userId, request.getNotificationType());
        
        return mapTypePreferenceToResponse(savedPreference);
    }
    
    /**
     * Check if a specific channel is enabled for a notification type.
     * Returns true if type-specific preference allows it, or if global preference allows it.
     */
    @Transactional(readOnly = true)
    public boolean isChannelEnabledForType(Long userId, NotificationType notificationType, String channel) {
        // First check type-specific preference
        NotificationTypePreference typePreference = typePreferenceRepository
                .findByUserIdAndNotificationType(userId, notificationType)
                .orElse(null);
        
        if (typePreference != null) {
            return isChannelEnabled(typePreference, channel);
        }
        
        // Fallback to global preference
        NotificationPreference globalPreference = preferenceRepository.findByUserId(userId)
                .orElse(null);
        
        if (globalPreference != null) {
            return isChannelEnabled(globalPreference, channel);
        }
        
        // Default: allow if no preferences exist
        return true;
    }
    
    /**
     * Helper method to check if a channel is enabled in a type preference.
     */
    private boolean isChannelEnabled(NotificationTypePreference preference, String channel) {
        return switch (channel.toLowerCase()) {
            case "email" -> Boolean.TRUE.equals(preference.getEmailEnabled());
            case "sms" -> Boolean.TRUE.equals(preference.getSmsEnabled());
            case "push" -> Boolean.TRUE.equals(preference.getPushEnabled());
            case "in_app", "in-app" -> Boolean.TRUE.equals(preference.getInAppEnabled());
            default -> true;
        };
    }
    
    /**
     * Helper method to check if a channel is enabled in a global preference.
     */
    private boolean isChannelEnabled(NotificationPreference preference, String channel) {
        return switch (channel.toLowerCase()) {
            case "email" -> Boolean.TRUE.equals(preference.getEmailEnabled());
            case "sms" -> Boolean.TRUE.equals(preference.getSmsEnabled());
            case "push" -> Boolean.TRUE.equals(preference.getPushEnabled());
            case "in_app", "in-app" -> Boolean.TRUE.equals(preference.getInAppEnabled());
            default -> true;
        };
    }
    
    /**
     * Map NotificationTypePreference entity to response DTO.
     */
    private NotificationTypePreferenceResponse mapTypePreferenceToResponse(NotificationTypePreference preference) {
        return NotificationTypePreferenceResponse.builder()
                .id(preference.getId())
                .userId(preference.getUserId())
                .notificationType(preference.getNotificationType())
                .emailEnabled(preference.getEmailEnabled())
                .smsEnabled(preference.getSmsEnabled())
                .pushEnabled(preference.getPushEnabled())
                .inAppEnabled(preference.getInAppEnabled())
                .createdAt(preference.getCreatedAt())
                .updatedAt(preference.getUpdatedAt())
                .build();
    }
}

