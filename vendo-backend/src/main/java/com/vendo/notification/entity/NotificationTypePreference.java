package com.vendo.notification.entity;

import com.vendo.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity for storing granular notification preferences per notification type.
 * Allows users to configure which channels (email, SMS, push, in-app) they want
 * to receive for each specific notification type.
 */
@Entity
@Table(name = "notification_type_preferences", indexes = {
    @Index(name = "idx_notification_type_prefs_user_type", columnList = "user_id,notification_type", unique = true),
    @Index(name = "idx_notification_type_prefs_user_id", columnList = "user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class NotificationTypePreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType notificationType;
    
    @Column(name = "email_enabled")
    @Builder.Default
    private Boolean emailEnabled = true;
    
    @Column(name = "sms_enabled")
    @Builder.Default
    private Boolean smsEnabled = false;
    
    @Column(name = "push_enabled")
    @Builder.Default
    private Boolean pushEnabled = true;
    
    @Column(name = "in_app_enabled")
    @Builder.Default
    private Boolean inAppEnabled = true;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

