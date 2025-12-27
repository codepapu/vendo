package com.vendo.integration.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "integration_sync_logs", indexes = {
    @Index(name = "idx_sync_logs_config_id", columnList = "integration_config_id"),
    @Index(name = "idx_sync_logs_status", columnList = "status"),
    @Index(name = "idx_sync_logs_type", columnList = "sync_type"),
    @Index(name = "idx_sync_logs_started_at", columnList = "started_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationSyncLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "integration_config_id", nullable = false)
    private IntegrationConfiguration integrationConfig;
    
    @Column(name = "sync_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private SyncType syncType;
    
    @Column(name = "sync_direction", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SyncDirection syncDirection;
    
    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private SyncStatus status;
    
    @Column(name = "records_processed")
    @Builder.Default
    private Integer recordsProcessed = 0;
    
    @Column(name = "records_successful")
    @Builder.Default
    private Integer recordsSuccessful = 0;
    
    @Column(name = "records_failed")
    @Builder.Default
    private Integer recordsFailed = 0;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "error_details", columnDefinition = "jsonb")
    private Map<String, Object> errorDetails;
    
    @CreatedDate
    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "duration_ms")
    private Long durationMs;
    
    public enum SyncType {
        ORDER_SYNC, INVOICE_SYNC, PAYMENT_SYNC, CUSTOMER_SYNC, PRODUCT_SYNC, INVENTORY_SYNC
    }
    
    public enum SyncDirection {
        INBOUND, OUTBOUND
    }
    
    public enum SyncStatus {
        PENDING, IN_PROGRESS, SUCCESS, FAILED, PARTIAL
    }
}

