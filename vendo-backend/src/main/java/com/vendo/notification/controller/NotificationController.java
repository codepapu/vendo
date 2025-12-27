package com.vendo.notification.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.notification.dto.request.NotificationPreferenceRequest;
import com.vendo.notification.dto.request.NotificationTypePreferenceRequest;
import com.vendo.notification.dto.request.SendNotificationRequest;
import com.vendo.notification.dto.response.NotificationPreferenceResponse;
import com.vendo.notification.dto.response.NotificationResponse;
import com.vendo.notification.dto.response.NotificationTypePreferenceResponse;
import com.vendo.notification.enums.NotificationType;
import com.vendo.notification.service.NotificationPreferenceService;
import com.vendo.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    private final NotificationPreferenceService preferenceService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getUserNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> notifications = notificationService.getUserNotifications(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }
    
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        Long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
    
    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        NotificationResponse notification = notificationService.sendNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Notification sent successfully", notification));
    }
    
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        NotificationResponse notification = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notification));
    }
    
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }
    
    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> getPreferences(
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        NotificationPreferenceResponse preferences = preferenceService.getPreferences(userId);
        return ResponseEntity.ok(ApiResponse.success(preferences));
    }
    
    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> updatePreferences(
            @Valid @RequestBody NotificationPreferenceRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        NotificationPreferenceResponse preferences = preferenceService.updatePreferences(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Notification preferences updated", preferences));
    }
    
    @GetMapping("/preferences/type/{notificationType}")
    public ResponseEntity<ApiResponse<NotificationTypePreferenceResponse>> getPreferenceByType(
            @PathVariable NotificationType notificationType,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        NotificationTypePreferenceResponse preference = preferenceService.getPreferenceByType(userId, notificationType);
        return ResponseEntity.ok(ApiResponse.success(preference));
    }
    
    @PutMapping("/preferences/type")
    public ResponseEntity<ApiResponse<NotificationTypePreferenceResponse>> updateTypePreference(
            @Valid @RequestBody NotificationTypePreferenceRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        NotificationTypePreferenceResponse preference = preferenceService.updateTypePreference(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Notification type preference updated", preference));
    }
}
