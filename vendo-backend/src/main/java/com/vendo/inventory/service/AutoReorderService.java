package com.vendo.inventory.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.common.notification.NotificationService;
import com.vendo.common.notification.NotificationRequest;
import com.vendo.inventory.dto.request.AutoReorderSettingsRequest;
import com.vendo.inventory.dto.response.AutoReorderSettingsResponse;
import com.vendo.inventory.entity.AutoReorderSettings;
import com.vendo.inventory.entity.Inventory;
import com.vendo.inventory.repository.AutoReorderSettingsRepository;
import com.vendo.inventory.repository.InventoryRepository;
import com.vendo.product.entity.Product;
import com.vendo.product.repository.ProductRepository;
import com.vendo.retailer.entity.Retailer;
import com.vendo.retailer.repository.RetailerRepository;
import com.vendo.inventory.entity.Warehouse;
import com.vendo.inventory.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoReorderService {
    
    private final AutoReorderSettingsRepository settingsRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final RetailerRepository retailerRepository;
    private final WarehouseRepository warehouseRepository;
    private final NotificationService notificationService;
    
    /**
     * Setup auto reorder settings
     */
    @Transactional
    public AutoReorderSettingsResponse setupAutoReorder(AutoReorderSettingsRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));
        
        Retailer retailer = null;
        if (request.getRetailerId() != null) {
            retailer = retailerRepository.findById(request.getRetailerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", request.getRetailerId()));
        }
        
        Warehouse warehouse = null;
        if (request.getWarehouseId() != null) {
            warehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", request.getWarehouseId()));
        }
        
        // Check if settings already exist
        AutoReorderSettings existing = settingsRepository
                .findByProductIdAndRetailerIdAndWarehouseId(
                        request.getProductId(),
                        request.getRetailerId(),
                        request.getWarehouseId())
                .orElse(null);
        
        if (existing != null) {
            // Update existing settings
            existing.setReorderPoint(request.getReorderPoint());
            existing.setReorderQuantity(request.getReorderQuantity());
            existing.setReorderStrategy(request.getReorderStrategy() != null ? 
                    request.getReorderStrategy() : "FIXED_QUANTITY");
            existing.setIsEnabled(request.getIsEnabled() != null ? request.getIsEnabled() : true);
            
            AutoReorderSettings saved = settingsRepository.save(existing);
            log.info("Auto reorder settings updated: ID={}", saved.getId());
            return mapToResponse(saved);
        }
        
        // Create new settings
        AutoReorderSettings settings = AutoReorderSettings.builder()
                .product(product)
                .retailer(retailer)
                .warehouse(warehouse)
                .reorderPoint(request.getReorderPoint())
                .reorderQuantity(request.getReorderQuantity())
                .reorderStrategy(request.getReorderStrategy() != null ? 
                        request.getReorderStrategy() : "FIXED_QUANTITY")
                .isEnabled(request.getIsEnabled() != null ? request.getIsEnabled() : true)
                .build();
        
        AutoReorderSettings saved = settingsRepository.save(settings);
        log.info("Auto reorder settings created: ID={}", saved.getId());
        return mapToResponse(saved);
    }
    
    /**
     * Update reorder settings
     */
    @Transactional
    public AutoReorderSettingsResponse updateReorderSettings(Long id, AutoReorderSettingsRequest request) {
        AutoReorderSettings settings = settingsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AutoReorderSettings", "id", id));
        
        if (request.getReorderPoint() != null) {
            settings.setReorderPoint(request.getReorderPoint());
        }
        if (request.getReorderQuantity() != null) {
            settings.setReorderQuantity(request.getReorderQuantity());
        }
        if (request.getReorderStrategy() != null) {
            settings.setReorderStrategy(request.getReorderStrategy());
        }
        if (request.getIsEnabled() != null) {
            settings.setIsEnabled(request.getIsEnabled());
        }
        
        AutoReorderSettings saved = settingsRepository.save(settings);
        log.info("Auto reorder settings updated: ID={}", saved.getId());
        return mapToResponse(saved);
    }
    
    /**
     * Process auto reorder - check inventory levels and create purchase orders
     */
    @Transactional
    public void processAutoReorder(Long retailerId, Long warehouseId) {
        List<AutoReorderSettings> settingsNeedingReorder = settingsRepository
                .findSettingsNeedingReorder(retailerId, warehouseId);
        
        for (AutoReorderSettings settings : settingsNeedingReorder) {
            try {
                processReorderForSettings(settings);
            } catch (Exception e) {
                log.error("Failed to process auto reorder for settings {}: {}", 
                        settings.getId(), e.getMessage(), e);
            }
        }
        
        log.info("Processed auto reorder for {} settings", settingsNeedingReorder.size());
    }
    
    /**
     * Scheduled job to process auto reorders (runs every hour)
     */
    @Scheduled(cron = "0 0 * * * ?") // Every hour
    @Transactional
    public void scheduledAutoReorder() {
        log.info("Running scheduled auto reorder check");
        processAutoReorder(null, null);
    }
    
    /**
     * Get reorder settings
     */
    @Transactional(readOnly = true)
    public AutoReorderSettingsResponse getReorderSettings(Long productId, Long retailerId, Long warehouseId) {
        AutoReorderSettings settings = settingsRepository
                .findByProductIdAndRetailerIdAndWarehouseId(productId, retailerId, warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("AutoReorderSettings", 
                        "productId=" + productId + ", retailerId=" + retailerId + ", warehouseId=" + warehouseId));
        return mapToResponse(settings);
    }
    
    @Transactional(readOnly = true)
    public List<AutoReorderSettingsResponse> getAllReorderSettings() {
        return settingsRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    private void processReorderForSettings(AutoReorderSettings settings) {
        // Get inventory
        Inventory inventory = inventoryRepository
                .findByProductIdAndRetailerIdAndWarehouseId(
                        settings.getProduct().getId(),
                        settings.getRetailer() != null ? settings.getRetailer().getId() : null,
                        settings.getWarehouse() != null ? settings.getWarehouse().getId() : null)
                .orElse(null);
        
        if (inventory == null) {
            log.warn("Inventory not found for auto reorder settings: {}", settings.getId());
            return;
        }
        
        // Check if reorder is needed
        if (inventory.getAvailableQuantity() > settings.getReorderPoint()) {
            return; // No reorder needed
        }
        
        // Calculate reorder quantity
        Integer reorderQuantity = calculateReorderQuantity(settings, inventory);
        
        // Create purchase order (would integrate with B2B purchase order service)
        // For now, just log and send notification
        log.info("Auto reorder triggered: Product={}, Quantity={}, Inventory={}", 
                settings.getProduct().getName(), reorderQuantity, inventory.getAvailableQuantity());
        
        // Send notification
        String message = String.format(
                "Auto reorder triggered for product %s. Reorder quantity: %d. Current inventory: %d",
                settings.getProduct().getName(),
                reorderQuantity,
                inventory.getAvailableQuantity()
        );
        
        if (settings.getRetailer() != null && settings.getRetailer().getUser() != null) {
            com.vendo.common.notification.NotificationRequest notificationRequest = com.vendo.common.notification.NotificationRequest.builder()
                    .userId(settings.getRetailer().getUser().getId())
                    .title("Auto Reorder Triggered")
                    .message(message)
                    .priority(com.vendo.common.notification.NotificationPriority.HIGH)
                    .build();
            notificationService.sendNotification(notificationRequest);
        }
        
        // Update last reorder time
        settings.setLastReorderAt(LocalDateTime.now());
        settingsRepository.save(settings);
    }
    
    private Integer calculateReorderQuantity(AutoReorderSettings settings, Inventory inventory) {
        if ("ECONOMIC_ORDER_QUANTITY".equals(settings.getReorderStrategy())) {
            // Calculate EOQ: sqrt((2 * demand * ordering_cost) / holding_cost)
            // Simplified calculation - in production would use actual costs
            int demand = settings.getReorderQuantity() * 12; // Annual demand estimate
            double orderingCost = 50.0; // Estimated ordering cost
            double holdingCost = 5.0; // Estimated holding cost per unit
            int eoq = (int) Math.sqrt((2 * demand * orderingCost) / holdingCost);
            return Math.max(eoq, settings.getReorderQuantity());
        }
        
        // Fixed quantity
        return settings.getReorderQuantity();
    }
    
    private AutoReorderSettingsResponse mapToResponse(AutoReorderSettings settings) {
        return AutoReorderSettingsResponse.builder()
                .id(settings.getId())
                .productId(settings.getProduct().getId())
                .productName(settings.getProduct().getName())
                .retailerId(settings.getRetailer() != null ? settings.getRetailer().getId() : null)
                .retailerName(settings.getRetailer() != null ? settings.getRetailer().getStoreName() : null)
                .warehouseId(settings.getWarehouse() != null ? settings.getWarehouse().getId() : null)
                .warehouseName(settings.getWarehouse() != null ? settings.getWarehouse().getName() : null)
                .reorderPoint(settings.getReorderPoint())
                .reorderQuantity(settings.getReorderQuantity())
                .reorderStrategy(settings.getReorderStrategy())
                .isEnabled(settings.getIsEnabled())
                .lastReorderAt(settings.getLastReorderAt())
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }
}

