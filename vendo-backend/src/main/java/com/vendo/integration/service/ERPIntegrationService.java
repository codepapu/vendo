package com.vendo.integration.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.integration.dto.response.ERPIntegrationResponse;
import com.vendo.integration.dto.response.SyncStatusResponse;
import com.vendo.integration.entity.IntegrationConfiguration;
import com.vendo.integration.entity.IntegrationSyncLog;
import com.vendo.integration.repository.IntegrationConfigurationRepository;
import com.vendo.integration.repository.IntegrationSyncLogRepository;
import com.vendo.integration.service.provider.ERPProviderFactory;
import com.vendo.integration.service.provider.ERPProviderService;
import com.vendo.inventory.entity.Inventory;
import com.vendo.inventory.repository.InventoryRepository;
import com.vendo.order.entity.Order;
import com.vendo.order.repository.OrderRepository;
import com.vendo.product.entity.Product;
import com.vendo.product.repository.ProductRepository;
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
public class ERPIntegrationService {
    
    private final IntegrationConfigurationRepository integrationConfigRepository;
    private final IntegrationSyncLogRepository syncLogRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final ERPProviderFactory providerFactory;
    
    /**
     * Sync inventory levels and product data to ERP
     */
    @Transactional
    public ERPIntegrationResponse syncToERP(Long integrationId) {
        log.info("Starting ERP sync for integration: {}", integrationId);
        
        IntegrationConfiguration config = integrationConfigRepository.findById(integrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Integration Configuration", "id", integrationId));
        
        if (!config.getIntegrationType().equals(IntegrationConfiguration.IntegrationType.ERP)) {
            throw new BadRequestException("Integration is not an ERP integration");
        }
        
        if (!config.getIsActive() || !config.getIsEnabled()) {
            throw new BadRequestException("Integration is not active or enabled");
        }
        
        IntegrationSyncLog syncLog = IntegrationSyncLog.builder()
                .integrationConfig(config)
                .syncType(IntegrationSyncLog.SyncType.PRODUCT_SYNC)
                .syncDirection(IntegrationSyncLog.SyncDirection.OUTBOUND)
                .status(IntegrationSyncLog.SyncStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .build();
        syncLog = syncLogRepository.save(syncLog);
        
        ERPProviderService providerService = providerFactory.getProviderService(config.getProvider());
        
        int productsSynced = 0;
        int inventorySynced = 0;
        int ordersSynced = 0;
        List<String> errors = new ArrayList<>();
        
        try {
            // Sync products
            List<Product> products = productRepository.findAll();
            for (Product product : products) {
                try {
                    Map<String, Object> result = providerService.syncProduct(product, config);
                    productsSynced++;
                    log.info("Synced product {} to {}", product.getSku(), config.getProvider());
                } catch (Exception e) {
                    log.error("Error syncing product {}: {}", product.getSku(), e.getMessage());
                    errors.add("Product " + product.getSku() + ": " + e.getMessage());
                }
            }
            
            // Sync inventory levels
            List<Inventory> inventoryItems = inventoryRepository.findAll();
            for (Inventory inventory : inventoryItems) {
                try {
                    Map<String, Object> result = providerService.syncInventory(inventory, config);
                    inventorySynced++;
                    log.info("Synced inventory {} to {}", inventory.getId(), config.getProvider());
                } catch (Exception e) {
                    log.error("Error syncing inventory {}: {}", inventory.getId(), e.getMessage());
                    errors.add("Inventory " + inventory.getId() + ": " + e.getMessage());
                }
            }
            
            // Sync orders
            List<Order> orders = orderRepository.findAll();
            for (Order order : orders) {
                try {
                    Map<String, Object> result = providerService.syncOrder(order, config);
                    ordersSynced++;
                    log.info("Synced order {} to {}", order.getCode(), config.getProvider());
                } catch (Exception e) {
                    log.error("Error syncing order {}: {}", order.getCode(), e.getMessage());
                    errors.add("Order " + order.getCode() + ": " + e.getMessage());
                }
            }
            
            syncLog.setStatus(errors.isEmpty() ? IntegrationSyncLog.SyncStatus.SUCCESS : IntegrationSyncLog.SyncStatus.PARTIAL);
            syncLog.setRecordsProcessed(productsSynced + inventorySynced + ordersSynced);
            syncLog.setRecordsSuccessful(productsSynced + inventorySynced + ordersSynced - errors.size());
            syncLog.setRecordsFailed(errors.size());
            syncLog.setCompletedAt(LocalDateTime.now());
            syncLog.setDurationMs(java.time.Duration.between(syncLog.getStartedAt(), syncLog.getCompletedAt()).toMillis());
            if (!errors.isEmpty()) {
                syncLog.setErrorMessage(String.join("; ", errors));
            }
            syncLogRepository.save(syncLog);
            
            config.setLastSyncAt(LocalDateTime.now());
            integrationConfigRepository.save(config);
            
            return ERPIntegrationResponse.builder()
                    .integrationId(integrationId)
                    .integrationName(config.getName())
                    .syncStatus(mapToSyncStatusResponse(syncLog))
                    .productsSynced(productsSynced)
                    .inventorySynced(inventorySynced)
                    .ordersSynced(ordersSynced)
                    .lastSyncAt(config.getLastSyncAt())
                    .errors(errors)
                    .success(errors.isEmpty())
                    .message("Sync completed: " + productsSynced + " products, " + inventorySynced + " inventory items, " + ordersSynced + " orders")
                    .build();
                    
        } catch (Exception e) {
            log.error("Error during ERP sync: ", e);
            syncLog.setStatus(IntegrationSyncLog.SyncStatus.FAILED);
            syncLog.setErrorMessage(e.getMessage());
            syncLog.setCompletedAt(LocalDateTime.now());
            syncLogRepository.save(syncLog);
            
            throw new BadRequestException("ERP sync failed: " + e.getMessage());
        }
    }
    
    /**
     * Import pricing and product information from ERP
     */
    @Transactional
    public ERPIntegrationResponse syncFromERP(Long integrationId) {
        log.info("Starting inbound ERP sync for integration: {}", integrationId);
        
        IntegrationConfiguration config = integrationConfigRepository.findById(integrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Integration Configuration", "id", integrationId));
        
        ERPProviderService providerService = providerFactory.getProviderService(config.getProvider());
        
        IntegrationSyncLog syncLog = IntegrationSyncLog.builder()
                .integrationConfig(config)
                .syncType(IntegrationSyncLog.SyncType.PRODUCT_SYNC)
                .syncDirection(IntegrationSyncLog.SyncDirection.INBOUND)
                .status(IntegrationSyncLog.SyncStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .build();
        syncLog = syncLogRepository.save(syncLog);
        
        int productsSynced = 0;
        int inventorySynced = 0;
        List<String> errors = new ArrayList<>();
        
        try {
            // Fetch products from ERP
            List<Map<String, Object>> externalProducts = providerService.fetchProducts(config);
            for (Map<String, Object> externalProduct : externalProducts) {
                try {
                    providerService.importProduct(externalProduct, config);
                    productsSynced++;
                } catch (Exception e) {
                    log.error("Error importing product: {}", e.getMessage());
                    errors.add("Product import error: " + e.getMessage());
                }
            }
            
            // Fetch inventory from ERP
            List<Map<String, Object>> externalInventory = providerService.fetchInventory(config);
            for (Map<String, Object> externalInv : externalInventory) {
                try {
                    providerService.importInventory(externalInv, config);
                    inventorySynced++;
                } catch (Exception e) {
                    log.error("Error importing inventory: {}", e.getMessage());
                    errors.add("Inventory import error: " + e.getMessage());
                }
            }
            
            syncLog.setStatus(errors.isEmpty() ? IntegrationSyncLog.SyncStatus.SUCCESS : IntegrationSyncLog.SyncStatus.PARTIAL);
            syncLog.setRecordsProcessed(externalProducts.size() + externalInventory.size());
            syncLog.setRecordsSuccessful(productsSynced + inventorySynced);
            syncLog.setRecordsFailed(errors.size());
            syncLog.setCompletedAt(LocalDateTime.now());
            syncLogRepository.save(syncLog);
            
            config.setLastSyncAt(LocalDateTime.now());
            integrationConfigRepository.save(config);
            
            return ERPIntegrationResponse.builder()
                    .integrationId(integrationId)
                    .integrationName(config.getName())
                    .syncStatus(mapToSyncStatusResponse(syncLog))
                    .productsSynced(productsSynced)
                    .inventorySynced(inventorySynced)
                    .lastSyncAt(config.getLastSyncAt())
                    .errors(errors)
                    .success(errors.isEmpty())
                    .message("Imported " + productsSynced + " products, " + inventorySynced + " inventory items from ERP")
                    .build();
                    
        } catch (Exception e) {
            log.error("Error during inbound ERP sync: ", e);
            syncLog.setStatus(IntegrationSyncLog.SyncStatus.FAILED);
            syncLog.setErrorMessage(e.getMessage());
            syncLog.setCompletedAt(LocalDateTime.now());
            syncLogRepository.save(syncLog);
            
            throw new BadRequestException("Inbound ERP sync failed: " + e.getMessage());
        }
    }
    
    /**
     * Handle webhook from ERP system
     */
    @Transactional
    public ERPIntegrationResponse handleWebhook(Long integrationId, String payload, String signature) {
        log.info("Handling ERP webhook for integration: {}", integrationId);
        
        IntegrationConfiguration config = integrationConfigRepository.findById(integrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Integration Configuration", "id", integrationId));
        
        ERPProviderService providerService = providerFactory.getProviderService(config.getProvider());
        
        if (!providerService.verifyWebhookSignature(payload, signature, config)) {
            throw new BadRequestException("Invalid webhook signature");
        }
        
        Map<String, Object> event = providerService.parseWebhookEvent(payload);
        String eventType = (String) event.get("eventType");
        
        List<String> errors = new ArrayList<>();
        
        try {
            switch (eventType) {
                case "product.updated":
                case "product.created":
                    providerService.handleProductWebhook(event, config);
                    break;
                case "inventory.updated":
                    providerService.handleInventoryWebhook(event, config);
                    break;
                case "order.created":
                case "order.updated":
                    providerService.handleOrderWebhook(event, config);
                    break;
                default:
                    log.warn("Unhandled webhook event type: {}", eventType);
            }
            
            return ERPIntegrationResponse.builder()
                    .integrationId(integrationId)
                    .integrationName(config.getName())
                    .success(true)
                    .message("Webhook processed successfully")
                    .build();
                    
        } catch (Exception e) {
            log.error("Error processing webhook: ", e);
            errors.add(e.getMessage());
            return ERPIntegrationResponse.builder()
                    .integrationId(integrationId)
                    .integrationName(config.getName())
                    .success(false)
                    .errors(errors)
                    .message("Webhook processing failed")
                    .build();
        }
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

