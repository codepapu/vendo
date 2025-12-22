package com.vendo.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_preferences", indexes = {
    @Index(name = "idx_notification_preferences_user_id", columnList = "user_id", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class NotificationPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
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
