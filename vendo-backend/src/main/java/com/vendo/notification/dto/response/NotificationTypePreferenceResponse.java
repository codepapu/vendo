package com.vendo.notification.dto.response;

import com.vendo.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for notification type preferences.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTypePreferenceResponse {
    
    private Long id;
    
    private Long userId;
    
    private NotificationType notificationType;
    
    private Boolean emailEnabled;
    
    private Boolean smsEnabled;
    
    private Boolean pushEnabled;
    
    private Boolean inAppEnabled;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}

