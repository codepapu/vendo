package com.vendo.integration.dto.request;

import com.vendo.integration.entity.IntegrationConfiguration;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationConfigRequest {
    
    @NotNull(message = "Integration type is required")
    private IntegrationConfiguration.IntegrationType integrationType;
    
    @NotNull(message = "Provider is required")
    private IntegrationConfiguration.IntegrationProvider provider;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String description;
    
    @Default
    private Boolean isActive = true;
    
    @Default
    private Boolean isEnabled = false;
    
    @NotNull(message = "Connection config is required")
    private Map<String, Object> connectionConfig;
    
    @Default
    private IntegrationConfiguration.SyncDirection syncDirection = IntegrationConfiguration.SyncDirection.BIDIRECTIONAL;
    
    @Default
    private Boolean autoSyncEnabled = false;
    
    private IntegrationConfiguration.SyncFrequency syncFrequency;
    
    private String webhookUrl;
    
    private String webhookSecret;
    
    @Default
    private Boolean webhookEnabled = false;
    
    private String oauthClientId;
    
    private String oauthClientSecret;
}

