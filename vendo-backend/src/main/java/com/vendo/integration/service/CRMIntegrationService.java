package com.vendo.integration.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.integration.dto.response.CRMIntegrationResponse;
import com.vendo.integration.dto.response.SyncStatusResponse;
import com.vendo.integration.entity.IntegrationConfiguration;
import com.vendo.integration.entity.IntegrationSyncLog;
import com.vendo.integration.repository.IntegrationConfigurationRepository;
import com.vendo.integration.repository.IntegrationSyncLogRepository;
import com.vendo.integration.service.provider.CRMProviderFactory;
import com.vendo.integration.service.provider.CRMProviderService;
import com.vendo.order.entity.Order;
import com.vendo.order.repository.OrderRepository;
import com.vendo.user.entity.User;
import com.vendo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CRMIntegrationService {
    
    private final IntegrationConfigurationRepository integrationConfigRepository;
    private final IntegrationSyncLogRepository syncLogRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CRMProviderFactory providerFactory;
    
    /**
     * Export customer data and order history to CRM
     */
    @Transactional
    public CRMIntegrationResponse syncToCRM(Long integrationId) {
        log.info("Starting CRM sync for integration: {}", integrationId);
        
        IntegrationConfiguration config = integrationConfigRepository.findById(integrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Integration Configuration", "id", integrationId));
        
        if (!config.getIntegrationType().equals(IntegrationConfiguration.IntegrationType.CRM)) {
            throw new BadRequestException("Integration is not a CRM integration");
        }
        
        if (!config.getIsActive() || !config.getIsEnabled()) {
            throw new BadRequestException("Integration is not active or enabled");
        }
        
        IntegrationSyncLog syncLog = IntegrationSyncLog.builder()
                .integrationConfig(config)
                .syncType(IntegrationSyncLog.SyncType.CUSTOMER_SYNC)
                .syncDirection(IntegrationSyncLog.SyncDirection.OUTBOUND)
                .status(IntegrationSyncLog.SyncStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .build();
        syncLog = syncLogRepository.save(syncLog);
        
        CRMProviderService providerService = providerFactory.getProviderService(config.getProvider());
        
        int contactsSynced = 0;
        int interactionsSynced = 0;
        List<String> errors = new ArrayList<>();
        
        try {
            // Sync customers as contacts
            List<User> customers = userRepository.findAll().stream()
                    .filter(user -> "CUSTOMER".equals(user.getUserType()))
                    .collect(java.util.stream.Collectors.toList());
            for (User customer : customers) {
                try {
                    Map<String, Object> result = providerService.syncContact(customer, config);
                    contactsSynced++;
                    
                    // Sync customer orders as interactions
                    List<Order> customerOrders = orderRepository.findByUserId(customer.getId());
                    for (Order order : customerOrders) {
                        try {
                            providerService.syncInteraction(customer, order, config);
                            interactionsSynced++;
                        } catch (Exception e) {
                            log.error("Error syncing interaction for order {}: {}", order.getCode(), e.getMessage());
                            errors.add("Interaction " + order.getCode() + ": " + e.getMessage());
                        }
                    }
                    
                    log.info("Synced customer {} to {}", customer.getEmail(), config.getProvider());
                } catch (Exception e) {
                    log.error("Error syncing customer {}: {}", customer.getEmail(), e.getMessage());
                    errors.add("Customer " + customer.getEmail() + ": " + e.getMessage());
                }
            }
            
            syncLog.setStatus(errors.isEmpty() ? IntegrationSyncLog.SyncStatus.SUCCESS : IntegrationSyncLog.SyncStatus.PARTIAL);
            syncLog.setRecordsProcessed(contactsSynced + interactionsSynced);
            syncLog.setRecordsSuccessful(contactsSynced + interactionsSynced - errors.size());
            syncLog.setRecordsFailed(errors.size());
            syncLog.setCompletedAt(LocalDateTime.now());
            syncLog.setDurationMs(java.time.Duration.between(syncLog.getStartedAt(), syncLog.getCompletedAt()).toMillis());
            if (!errors.isEmpty()) {
                syncLog.setErrorMessage(String.join("; ", errors));
            }
            syncLogRepository.save(syncLog);
            
            config.setLastSyncAt(LocalDateTime.now());
            integrationConfigRepository.save(config);
            
            return CRMIntegrationResponse.builder()
                    .integrationId(integrationId)
                    .integrationName(config.getName())
                    .syncStatus(mapToSyncStatusResponse(syncLog))
                    .contactsSynced(contactsSynced)
                    .interactionsSynced(interactionsSynced)
                    .lastSyncAt(config.getLastSyncAt())
                    .errors(errors)
                    .success(errors.isEmpty())
                    .message("Sync completed: " + contactsSynced + " contacts, " + interactionsSynced + " interactions")
                    .build();
                    
        } catch (Exception e) {
            log.error("Error during CRM sync: ", e);
            syncLog.setStatus(IntegrationSyncLog.SyncStatus.FAILED);
            syncLog.setErrorMessage(e.getMessage());
            syncLog.setCompletedAt(LocalDateTime.now());
            syncLogRepository.save(syncLog);
            
            throw new BadRequestException("CRM sync failed: " + e.getMessage());
        }
    }
    
    /**
     * Import CRM contacts as customers
     */
    @Transactional
    public CRMIntegrationResponse syncFromCRM(Long integrationId) {
        log.info("Starting inbound CRM sync for integration: {}", integrationId);
        
        IntegrationConfiguration config = integrationConfigRepository.findById(integrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Integration Configuration", "id", integrationId));
        
        CRMProviderService providerService = providerFactory.getProviderService(config.getProvider());
        
        IntegrationSyncLog syncLog = IntegrationSyncLog.builder()
                .integrationConfig(config)
                .syncType(IntegrationSyncLog.SyncType.CUSTOMER_SYNC)
                .syncDirection(IntegrationSyncLog.SyncDirection.INBOUND)
                .status(IntegrationSyncLog.SyncStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .build();
        syncLog = syncLogRepository.save(syncLog);
        
        int contactsSynced = 0;
        List<String> errors = new ArrayList<>();
        
        try {
            // Fetch contacts from CRM
            List<Map<String, Object>> externalContacts = providerService.fetchContacts(config);
            
            for (Map<String, Object> externalContact : externalContacts) {
                try {
                    providerService.importContact(externalContact, config);
                    contactsSynced++;
                } catch (Exception e) {
                    log.error("Error importing contact: {}", e.getMessage());
                    errors.add("Contact import error: " + e.getMessage());
                }
            }
            
            syncLog.setStatus(errors.isEmpty() ? IntegrationSyncLog.SyncStatus.SUCCESS : IntegrationSyncLog.SyncStatus.PARTIAL);
            syncLog.setRecordsProcessed(externalContacts.size());
            syncLog.setRecordsSuccessful(contactsSynced);
            syncLog.setRecordsFailed(errors.size());
            syncLog.setCompletedAt(LocalDateTime.now());
            syncLogRepository.save(syncLog);
            
            config.setLastSyncAt(LocalDateTime.now());
            integrationConfigRepository.save(config);
            
            return CRMIntegrationResponse.builder()
                    .integrationId(integrationId)
                    .integrationName(config.getName())
                    .syncStatus(mapToSyncStatusResponse(syncLog))
                    .contactsSynced(contactsSynced)
                    .lastSyncAt(config.getLastSyncAt())
                    .errors(errors)
                    .success(errors.isEmpty())
                    .message("Imported " + contactsSynced + " contacts from CRM")
                    .build();
                    
        } catch (Exception e) {
            log.error("Error during inbound CRM sync: ", e);
            syncLog.setStatus(IntegrationSyncLog.SyncStatus.FAILED);
            syncLog.setErrorMessage(e.getMessage());
            syncLog.setCompletedAt(LocalDateTime.now());
            syncLogRepository.save(syncLog);
            
            throw new BadRequestException("Inbound CRM sync failed: " + e.getMessage());
        }
    }
    
    /**
     * Handle webhook from CRM system
     */
    @Transactional
    public CRMIntegrationResponse handleWebhook(Long integrationId, String payload, String signature) {
        log.info("Handling CRM webhook for integration: {}", integrationId);
        
        IntegrationConfiguration config = integrationConfigRepository.findById(integrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Integration Configuration", "id", integrationId));
        
        CRMProviderService providerService = providerFactory.getProviderService(config.getProvider());
        
        if (!providerService.verifyWebhookSignature(payload, signature, config)) {
            throw new BadRequestException("Invalid webhook signature");
        }
        
        Map<String, Object> event = providerService.parseWebhookEvent(payload);
        String eventType = (String) event.get("eventType");
        
        List<String> errors = new ArrayList<>();
        
        try {
            switch (eventType) {
                case "contact.created":
                case "contact.updated":
                    providerService.handleContactWebhook(event, config);
                    break;
                case "deal.created":
                case "deal.updated":
                    providerService.handleDealWebhook(event, config);
                    break;
                default:
                    log.warn("Unhandled webhook event type: {}", eventType);
            }
            
            return CRMIntegrationResponse.builder()
                    .integrationId(integrationId)
                    .integrationName(config.getName())
                    .success(true)
                    .message("Webhook processed successfully")
                    .build();
                    
        } catch (Exception e) {
            log.error("Error processing webhook: ", e);
            errors.add(e.getMessage());
            return CRMIntegrationResponse.builder()
                    .integrationId(integrationId)
                    .integrationName(config.getName())
                    .success(false)
                    .errors(errors)
                    .message("Webhook processing failed")
                    .build();
        }
    }
    
    /**
     * Track customer interaction lifecycle
     */
    @Transactional
    public void trackInteraction(Long integrationId, User customer, Order order, String interactionType) {
        IntegrationConfiguration config = integrationConfigRepository.findById(integrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Integration Configuration", "id", integrationId));
        
        CRMProviderService providerService = providerFactory.getProviderService(config.getProvider());
        providerService.trackInteraction(customer, order, interactionType, config);
    }
    
    private SyncStatusResponse mapToSyncStatusResponse(IntegrationSyncLog syncLog) {
        return SyncStatusResponse.builder()
                .syncLogId(syncLog.getId())
                .syncType(syncLog.getSyncType())
                .syncDirection(syncLog.getSyncDirection())
                .status(syncLog.getStatus())
                .recordsProcessed(syncLog.getRecordsProcessed())
                .recordsSuccessful(syncLog.getRecordsSuccessful())
                .recordsFailed(syncLog.getRecordsFailed())
                .errorMessage(syncLog.getErrorMessage())
                .startedAt(syncLog.getStartedAt())
                .completedAt(syncLog.getCompletedAt())
                .durationMs(syncLog.getDurationMs())
                .build();
    }
}

