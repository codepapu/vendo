package com.vendo.user.dto.response;

import com.vendo.content.dto.response.MediaFileResponse;
import com.vendo.notification.dto.response.NotificationPreferenceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    
    private Long id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String phone;
    private String userType;
    private Boolean isActive;
    private Boolean isVerified;
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Avatar information
    private MediaFileResponse avatar;
    private String avatarUrl;
    
    // Notification preferences
    private NotificationPreferenceResponse notificationPreferences;
}
