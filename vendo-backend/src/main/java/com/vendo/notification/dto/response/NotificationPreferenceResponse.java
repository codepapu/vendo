package com.vendo.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for notification preferences.
 * Includes both global channel preferences and granular type-specific preferences.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceResponse {
    
    private Long id;
    private Long userId;
    
    // Global channel preferences (fallback when type-specific preferences don't exist)
    private Boolean emailEnabled;
    private Boolean smsEnabled;
    private Boolean pushEnabled;
    private Boolean inAppEnabled;
    
    // Granular preferences per notification type
    private List<NotificationTypePreferenceResponse> typePreferences;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
