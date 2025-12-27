package com.vendo.integration.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "integration_sync_records", indexes = {
    @Index(name = "idx_sync_records_log_id", columnList = "sync_log_id"),
    @Index(name = "idx_sync_records_entity", columnList = "entity_type,entity_id"),
    @Index(name = "idx_sync_records_external_id", columnList = "external_id"),
    @Index(name = "idx_sync_records_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationSyncRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sync_log_id", nullable = false)
    private IntegrationSyncLog syncLog;
    
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType; // ORDER, INVOICE, PAYMENT, CUSTOMER, PRODUCT, INVENTORY
    
    @Column(name = "entity_id", nullable = false)
    private Long entityId;
    
    @Column(name = "external_id", length = 255)
    private String externalId;
    
    @Column(name = "external_reference", length = 255)
    private String externalReference;
    
    @Column(name = "sync_direction", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SyncDirection syncDirection;
    
    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private RecordStatus status;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "error_details", columnDefinition = "jsonb")
    private Map<String, Object> errorDetails;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "synced_data", columnDefinition = "jsonb")
    private Map<String, Object> syncedData;
    
    @Column(name = "synced_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime syncedAt = LocalDateTime.now();
    
    public enum SyncDirection {
        INBOUND, OUTBOUND
    }
    
    public enum RecordStatus {
        PENDING, SYNCED, FAILED, CONFLICT
    }
}

