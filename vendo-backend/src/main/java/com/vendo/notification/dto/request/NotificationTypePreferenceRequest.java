package com.vendo.notification.dto.request;

import com.vendo.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating notification type preferences.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTypePreferenceRequest {
    
    private NotificationType notificationType;
    
    private Boolean emailEnabled;
    
    private Boolean smsEnabled;
    
    private Boolean pushEnabled;
    
    private Boolean inAppEnabled;
}

