package com.vendo.notification.dto.request;

import com.vendo.notification.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for sending notifications.
 * Supports sending via multiple channels based on user preferences.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    /**
     * The category/type of notification (e.g., ORDER_CONFIRMED, PROMOTION, etc.)
     * Used to check granular user preferences.
     */
    private NotificationType notificationType;
    
    /**
     * The delivery channel (email, sms, push, in_app).
     * If not specified, will send via all enabled channels based on user preferences.
     */
    private String channel; // email, sms, push, in_app
    
    /**
     * List of channels to send via. If specified, overrides single channel.
     * If not specified, will use user preferences to determine channels.
     */
    private List<String> channels;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Message is required")
    private String message;
    
    private Map<String, Object> data;
}
