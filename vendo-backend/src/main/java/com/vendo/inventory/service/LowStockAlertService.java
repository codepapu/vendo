package com.vendo.inventory.service;

import com.vendo.common.notification.MultiChannelNotificationService;
import com.vendo.inventory.dto.response.LowStockAlertResponse;
import com.vendo.inventory.entity.Inventory;
import com.vendo.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LowStockAlertService {
    
    private final InventoryRepository inventoryRepository;
    private final MultiChannelNotificationService notificationService;
    
    /**
     * Check for low stock items
     */
    @Transactional(readOnly = true)
    public LowStockAlertResponse checkLowStock(Long retailerId, Long warehouseId) {
        Page<Inventory> lowStockItems = inventoryRepository.findLowStockItems(retailerId, warehouseId, Pageable.unpaged());
        
        List<LowStockAlertResponse.LowStockProduct> products = new ArrayList<>();
        int warningCount = 0;
        int criticalCount = 0;
        
        for (Inventory inventory : lowStockItems) {
            String alertLevel = determineAlertLevel(inventory);
            
            if ("CRITICAL".equals(alertLevel)) {
                criticalCount++;
            } else {
                warningCount++;
            }
            
            LowStockAlertResponse.LowStockProduct product = LowStockAlertResponse.LowStockProduct.builder()
                    .inventoryId(inventory.getId())
                    .productId(inventory.getProduct().getId())
                    .productName(inventory.getProduct().getName())
                    .sku(inventory.getProduct().getSku())
                    .retailerId(inventory.getRetailer() != null ? inventory.getRetailer().getId() : null)
                    .retailerName(inventory.getRetailer() != null ? inventory.getRetailer().getStoreName() : null)
                    .warehouseId(inventory.getWarehouse() != null ? inventory.getWarehouse().getId() : null)
                    .warehouseName(inventory.getWarehouse() != null ? inventory.getWarehouse().getName() : null)
                    .currentStock(inventory.getQuantity())
                    .availableQuantity(inventory.getAvailableQuantity())
                    .lowStockThreshold(inventory.getLowStockThreshold())
                    .reorderPoint(inventory.getReorderPoint())
                    .alertLevel(alertLevel)
                    .build();
            
            products.add(product);
        }
        
        return LowStockAlertResponse.builder()
                .products(products)
                .totalCount(products.size())
                .warningCount(warningCount)
                .criticalCount(criticalCount)
                .build();
    }
    
    /**
     * Send low stock alerts
     */
    @Transactional
    public void sendLowStockAlerts(Long retailerId, Long warehouseId) {
        LowStockAlertResponse alertResponse = checkLowStock(retailerId, warehouseId);
        
        for (LowStockAlertResponse.LowStockProduct product : alertResponse.getProducts()) {
            String message = String.format(
                    "Low stock alert for %s: Current stock: %d, Threshold: %d, Alert Level: %s",
                    product.getProductName(),
                    product.getCurrentStock(),
                    product.getLowStockThreshold(),
                    product.getAlertLevel()
            );
            
            // Send to retailer if applicable
            if (product.getRetailerId() != null) {
                // Would need to get retailer user ID
                // For now, just log
                log.info("Low stock alert for retailer {}: {}", product.getRetailerId(), message);
            }
            
            // Send to admins
            com.vendo.common.notification.NotificationRequest notificationRequest = 
                    com.vendo.common.notification.NotificationRequest.builder()
                    .recipient(null) // Admin notification - would need admin user IDs
                    .title("Low Stock Alert")
                    .message(message)
                    .priority("CRITICAL".equals(product.getAlertLevel()) ? 
                            com.vendo.common.notification.NotificationPriority.HIGH :
                            com.vendo.common.notification.NotificationPriority.NORMAL)
                    .build();
            notificationService.sendNotification(notificationRequest);
        }
        
        log.info("Sent {} low stock alerts", alertResponse.getTotalCount());
    }
    
    /**
     * Get low stock products
     */
    @Transactional(readOnly = true)
    public List<LowStockAlertResponse.LowStockProduct> getLowStockProducts(Long retailerId, Long warehouseId, Pageable pageable) {
        Page<Inventory> lowStockItems = inventoryRepository.findLowStockItems(retailerId, warehouseId, pageable);
        
        return lowStockItems.stream()
                .map(inventory -> LowStockAlertResponse.LowStockProduct.builder()
                        .inventoryId(inventory.getId())
                        .productId(inventory.getProduct().getId())
                        .productName(inventory.getProduct().getName())
                        .sku(inventory.getProduct().getSku())
                        .retailerId(inventory.getRetailer() != null ? inventory.getRetailer().getId() : null)
                        .retailerName(inventory.getRetailer() != null ? inventory.getRetailer().getStoreName() : null)
                        .warehouseId(inventory.getWarehouse() != null ? inventory.getWarehouse().getId() : null)
                        .warehouseName(inventory.getWarehouse() != null ? inventory.getWarehouse().getName() : null)
                        .currentStock(inventory.getQuantity())
                        .availableQuantity(inventory.getAvailableQuantity())
                        .lowStockThreshold(inventory.getLowStockThreshold())
                        .reorderPoint(inventory.getReorderPoint())
                        .alertLevel(determineAlertLevel(inventory))
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * Update alert thresholds
     */
    @Transactional
    public void updateAlertThresholds(Long inventoryId, Integer lowStockThreshold, Integer reorderPoint) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new com.vendo.common.exception.ResourceNotFoundException("Inventory", "id", inventoryId));
        
        if (lowStockThreshold != null) {
            inventory.setLowStockThreshold(lowStockThreshold);
        }
        if (reorderPoint != null) {
            inventory.setReorderPoint(reorderPoint);
        }
        
        inventoryRepository.save(inventory);
        log.info("Updated alert thresholds for inventory ID: {}", inventoryId);
    }
    
    /**
     * Scheduled job to check and send low stock alerts (runs daily)
     */
    @Scheduled(cron = "0 0 9 * * ?") // Every day at 9 AM
    @Transactional
    public void scheduledLowStockCheck() {
        log.info("Running scheduled low stock check");
        sendLowStockAlerts(null, null);
    }
    
    private String determineAlertLevel(Inventory inventory) {
        int available = inventory.getAvailableQuantity() != null ? inventory.getAvailableQuantity() : 0;
        int threshold = inventory.getLowStockThreshold();
        int reorderPoint = inventory.getReorderPoint();
        
        if (available <= reorderPoint) {
            return "CRITICAL";
        } else if (available <= threshold) {
            return "WARNING";
        }
        
        return "WARNING"; // Default
    }
}

