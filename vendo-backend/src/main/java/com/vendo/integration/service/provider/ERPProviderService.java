package com.vendo.integration.service.provider;

import com.vendo.integration.entity.IntegrationConfiguration;
import com.vendo.inventory.entity.Inventory;
import com.vendo.order.entity.Order;
import com.vendo.product.entity.Product;

import java.util.List;
import java.util.Map;

public interface ERPProviderService {
    
    /**
     * Sync product to ERP
     */
    Map<String, Object> syncProduct(Product product, IntegrationConfiguration config);
    
    /**
     * Sync inventory to ERP
     */
    Map<String, Object> syncInventory(Inventory inventory, IntegrationConfiguration config);
    
    /**
     * Sync order to ERP
     */
    Map<String, Object> syncOrder(Order order, IntegrationConfiguration config);
    
    /**
     * Verify webhook signature
     */
    boolean verifyWebhookSignature(String payload, String signature, IntegrationConfiguration config);
    
    /**
     * Parse webhook event
     */
    Map<String, Object> parseWebhookEvent(String payload);
    
    /**
     * Handle product webhook event
     */
    void handleProductWebhook(Map<String, Object> event, IntegrationConfiguration config);
    
    /**
     * Handle inventory webhook event
     */
    void handleInventoryWebhook(Map<String, Object> event, IntegrationConfiguration config);
    
    /**
     * Handle order webhook event
     */
    void handleOrderWebhook(Map<String, Object> event, IntegrationConfiguration config);
    
    /**
     * Fetch products from ERP (inbound sync)
     */
    List<Map<String, Object>> fetchProducts(IntegrationConfiguration config);
    
    /**
     * Import product from ERP
     */
    void importProduct(Map<String, Object> externalProduct, IntegrationConfiguration config);
    
    /**
     * Fetch inventory from ERP (inbound sync)
     */
    List<Map<String, Object>> fetchInventory(IntegrationConfiguration config);
    
    /**
     * Import inventory from ERP
     */
    void importInventory(Map<String, Object> externalInventory, IntegrationConfiguration config);
}

