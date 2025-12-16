package com.nexus.analytics.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "analytics_events", indexes = {
    @Index(name = "idx_analytics_events_type", columnList = "event_type"),
    @Index(name = "idx_analytics_events_user_id", columnList = "user_id"),
    @Index(name = "idx_analytics_events_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AnalyticsEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType; // page_view, purchase, add_to_cart, etc.
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "session_id", length = 191)
    private String sessionId;
    
    @Column(columnDefinition = "TEXT")
    private String properties; // JSON string
    
    @Column(length = 191)
    private String ipAddress;
    
    @Column(length = 500)
    private String userAgent;
    
    @Column(length = 191)
    private String referrer;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
