package com.vendo.integration.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountingIntegrationResponse {
    
    private Long integrationId;
    
    private String integrationName;
    
    private SyncStatusResponse syncStatus;
    
    private Integer invoicesSynced;
    
    private Integer paymentsSynced;
    
    private Integer ordersSynced;
    
    private LocalDateTime lastSyncAt;
    
    private List<String> errors;
    
    private Boolean success;
    
    private String message;
}

