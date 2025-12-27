package com.vendo.integration.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.integration.dto.request.IntegrationConfigRequest;
import com.vendo.integration.dto.response.IntegrationResponse;
import com.vendo.integration.entity.IntegrationConfiguration;
import com.vendo.integration.repository.IntegrationConfigurationRepository;
import com.vendo.user.entity.User;
import com.vendo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntegrationConfigService {
    
    private final IntegrationConfigurationRepository integrationConfigRepository;
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public Page<IntegrationResponse> getAllConfigurations(Pageable pageable) {
        return integrationConfigRepository.findAll(pageable)
                .map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public IntegrationResponse getConfigurationById(Long id) {
        IntegrationConfiguration config = integrationConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Integration Configuration", "id", id));
        return mapToResponse(config);
    }
    
    @Transactional(readOnly = true)
    public Page<IntegrationResponse> getConfigurationsByType(
            IntegrationConfiguration.IntegrationType type, 
            Pageable pageable) {
        return integrationConfigRepository.findByIntegrationType(type, pageable)
                .map(this::mapToResponse);
    }
    
    @Transactional
    public IntegrationResponse createConfiguration(IntegrationConfigRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Check for duplicate
        integrationConfigRepository.findByIntegrationTypeAndProviderAndName(
                request.getIntegrationType(), 
                request.getProvider(), 
                request.getName())
                .ifPresent(config -> {
                    throw new BadRequestException("Integration with same type, provider, and name already exists");
                });
        
        IntegrationConfiguration config = IntegrationConfiguration.builder()
                .integrationType(request.getIntegrationType())
                .provider(request.getProvider())
                .name(request.getName())
                .description(request.getDescription())
                .isActive(request.getIsActive())
                .isEnabled(request.getIsEnabled())
                .connectionConfig(request.getConnectionConfig())
                .syncDirection(request.getSyncDirection())
                .autoSyncEnabled(request.getAutoSyncEnabled())
                .syncFrequency(request.getSyncFrequency())
                .webhookUrl(request.getWebhookUrl())
                .webhookSecret(request.getWebhookSecret())
                .webhookEnabled(request.getWebhookEnabled())
                .oauthClientId(request.getOauthClientId())
                .oauthClientSecret(request.getOauthClientSecret())
                .createdBy(user)
                .build();
        
        config = integrationConfigRepository.save(config);
        log.info("Created integration configuration: {}", config.getName());
        
        return mapToResponse(config);
    }
    
    @Transactional
    public IntegrationResponse updateConfiguration(Long id, IntegrationConfigRequest request) {
        IntegrationConfiguration config = integrationConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Integration Configuration", "id", id));
        
        config.setName(request.getName());
        config.setDescription(request.getDescription());
        config.setIsActive(request.getIsActive());
        config.setIsEnabled(request.getIsEnabled());
        config.setConnectionConfig(request.getConnectionConfig());
        config.setSyncDirection(request.getSyncDirection());
        config.setAutoSyncEnabled(request.getAutoSyncEnabled());
        config.setSyncFrequency(request.getSyncFrequency());
        config.setWebhookUrl(request.getWebhookUrl());
        config.setWebhookSecret(request.getWebhookSecret());
        config.setWebhookEnabled(request.getWebhookEnabled());
        config.setOauthClientId(request.getOauthClientId());
        config.setOauthClientSecret(request.getOauthClientSecret());
        
        config = integrationConfigRepository.save(config);
        log.info("Updated integration configuration: {}", config.getName());
        
        return mapToResponse(config);
    }
    
    @Transactional
    public void deleteConfiguration(Long id) {
        IntegrationConfiguration config = integrationConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Integration Configuration", "id", id));
        
        integrationConfigRepository.delete(config);
        log.info("Deleted integration configuration: {}", config.getName());
    }
    
    @Transactional
    public IntegrationResponse enableConfiguration(Long id) {
        IntegrationConfiguration config = integrationConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Integration Configuration", "id", id));
        
        config.setIsEnabled(true);
        config = integrationConfigRepository.save(config);
        
        return mapToResponse(config);
    }
    
    @Transactional
    public IntegrationResponse disableConfiguration(Long id) {
        IntegrationConfiguration config = integrationConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Integration Configuration", "id", id));
        
        config.setIsEnabled(false);
        config = integrationConfigRepository.save(config);
        
        return mapToResponse(config);
    }
    
    private IntegrationResponse mapToResponse(IntegrationConfiguration config) {
        return IntegrationResponse.builder()
                .id(config.getId())
                .integrationType(config.getIntegrationType())
                .provider(config.getProvider())
                .name(config.getName())
                .description(config.getDescription())
                .isActive(config.getIsActive())
                .isEnabled(config.getIsEnabled())
                .syncDirection(config.getSyncDirection())
                .autoSyncEnabled(config.getAutoSyncEnabled())
                .syncFrequency(config.getSyncFrequency())
                .lastSyncAt(config.getLastSyncAt())
                .nextSyncAt(config.getNextSyncAt())
                .webhookEnabled(config.getWebhookEnabled())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}

