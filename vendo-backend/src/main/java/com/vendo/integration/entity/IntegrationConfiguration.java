package com.vendo.integration.entity;

import com.vendo.common.entity.BaseEntity;
import com.vendo.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "integration_configurations", indexes = {
    @Index(name = "idx_integration_configs_type", columnList = "integration_type"),
    @Index(name = "idx_integration_configs_provider", columnList = "provider"),
    @Index(name = "idx_integration_configs_active", columnList = "is_active"),
    @Index(name = "idx_integration_configs_enabled", columnList = "is_enabled")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class IntegrationConfiguration extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "integration_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private IntegrationType integrationType;
    
    @Column(name = "provider", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private IntegrationProvider provider;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "is_enabled")
    @Builder.Default
    private Boolean isEnabled = false;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "connection_config", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> connectionConfig;
    
    @Column(name = "sync_direction", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SyncDirection syncDirection = SyncDirection.BIDIRECTIONAL;
    
    @Column(name = "auto_sync_enabled")
    @Builder.Default
    private Boolean autoSyncEnabled = false;
    
    @Column(name = "sync_frequency", length = 50)
    @Enumerated(EnumType.STRING)
    private SyncFrequency syncFrequency;
    
    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;
    
    @Column(name = "next_sync_at")
    private LocalDateTime nextSyncAt;
    
    @Column(name = "webhook_url", length = 500)
    private String webhookUrl;
    
    @Column(name = "webhook_secret", length = 255)
    private String webhookSecret;
    
    @Column(name = "webhook_enabled")
    @Builder.Default
    private Boolean webhookEnabled = false;
    
    @Column(name = "oauth_client_id", length = 255)
    private String oauthClientId;
    
    @Column(name = "oauth_client_secret", length = 255)
    private String oauthClientSecret;
    
    @Column(name = "oauth_access_token", columnDefinition = "TEXT")
    private String oauthAccessToken;
    
    @Column(name = "oauth_refresh_token", columnDefinition = "TEXT")
    private String oauthRefreshToken;
    
    @Column(name = "oauth_token_expires_at")
    private LocalDateTime oauthTokenExpiresAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    public enum IntegrationType {
        ACCOUNTING, CRM, ERP
    }
    
    public enum IntegrationProvider {
        // Accounting
        QUICKBOOKS, XERO, SAGE,
        // CRM
        SALESFORCE, HUBSPOT,
        // ERP
        SAP, ORACLE, MICROSOFT_DYNAMICS
    }
    
    public enum SyncDirection {
        INBOUND, OUTBOUND, BIDIRECTIONAL
    }
    
    public enum SyncFrequency {
        HOURLY, DAILY, WEEKLY, MANUAL
    }
}

