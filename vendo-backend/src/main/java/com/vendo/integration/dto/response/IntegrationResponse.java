package com.vendo.integration.dto.response;

import com.vendo.integration.entity.IntegrationConfiguration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationResponse {
    
    private Long id;
    
    private IntegrationConfiguration.IntegrationType integrationType;
    
    private IntegrationConfiguration.IntegrationProvider provider;
    
    private String name;
    
    private String description;
    
    private Boolean isActive;
    
    private Boolean isEnabled;
    
    private IntegrationConfiguration.SyncDirection syncDirection;
    
    private Boolean autoSyncEnabled;
    
    private IntegrationConfiguration.SyncFrequency syncFrequency;
    
    private LocalDateTime lastSyncAt;
    
    private LocalDateTime nextSyncAt;
    
    private Boolean webhookEnabled;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}

