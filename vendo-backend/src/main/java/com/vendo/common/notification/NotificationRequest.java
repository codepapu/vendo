package com.vendo.common.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private String recipient; // Email, phone, or user ID
    private String subject;
    private String title;
    private String message;
    private String template; // Template name for email
    private Map<String, Object> data; // Template variables
    private List<NotificationChannel> channels; // If null, send via all channels
    private NotificationPriority priority;
    private Long userId; // Optional: for user-specific notifications
}
