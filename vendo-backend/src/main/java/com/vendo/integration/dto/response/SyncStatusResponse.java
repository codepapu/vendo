package com.vendo.integration.dto.response;

import com.vendo.integration.entity.IntegrationSyncLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncStatusResponse {
    
    private Long syncLogId;
    
    private IntegrationSyncLog.SyncType syncType;
    
    private IntegrationSyncLog.SyncDirection syncDirection;
    
    private IntegrationSyncLog.SyncStatus status;
    
    private Integer recordsProcessed;
    
    private Integer recordsSuccessful;
    
    private Integer recordsFailed;
    
    private String errorMessage;
    
    private LocalDateTime startedAt;
    
    private LocalDateTime completedAt;
    
    private Long durationMs;
}

