package com.vendo.integration.service.provider;

import com.vendo.integration.entity.IntegrationConfiguration;
import com.vendo.order.entity.Order;
import com.vendo.user.entity.User;

import java.util.List;
import java.util.Map;

public interface CRMProviderService {
    
    /**
     * Sync contact (customer) to CRM
     */
    Map<String, Object> syncContact(User customer, IntegrationConfiguration config);
    
    /**
     * Sync interaction (order) to CRM
     */
    Map<String, Object> syncInteraction(User customer, Order order, IntegrationConfiguration config);
    
    /**
     * Track customer interaction lifecycle
     */
    void trackInteraction(User customer, Order order, String interactionType, IntegrationConfiguration config);
    
    /**
     * Verify webhook signature
     */
    boolean verifyWebhookSignature(String payload, String signature, IntegrationConfiguration config);
    
    /**
     * Parse webhook event
     */
    Map<String, Object> parseWebhookEvent(String payload);
    
    /**
     * Handle contact webhook event
     */
    void handleContactWebhook(Map<String, Object> event, IntegrationConfiguration config);
    
    /**
     * Handle deal webhook event
     */
    void handleDealWebhook(Map<String, Object> event, IntegrationConfiguration config);
    
    /**
     * Fetch contacts from CRM (inbound sync)
     */
    List<Map<String, Object>> fetchContacts(IntegrationConfiguration config);
    
    /**
     * Import contact from CRM
     */
    void importContact(Map<String, Object> externalContact, IntegrationConfiguration config);
}

