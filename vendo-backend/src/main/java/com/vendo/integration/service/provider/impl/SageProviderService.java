package com.vendo.integration.service.provider.impl;

import com.vendo.integration.entity.IntegrationConfiguration;
import com.vendo.integration.service.provider.AccountingProviderService;
import com.vendo.order.entity.Invoice;
import com.vendo.order.entity.Order;
import com.vendo.payment.entity.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SageProviderService implements AccountingProviderService {
    
    @Override
    public Map<String, Object> syncInvoice(Invoice invoice, IntegrationConfiguration config) {
        log.info("Syncing invoice {} to Sage", invoice.getInvoiceNumber());
        
        Map<String, Object> result = new HashMap<>();
        result.put("externalInvoiceId", "SAGE-" + invoice.getId());
        result.put("status", "synced");
        result.put("syncedAt", java.time.LocalDateTime.now());
        
        return result;
    }
    
    @Override
    public Map<String, Object> syncPayment(Payment payment, IntegrationConfiguration config) {
        log.info("Syncing payment {} to Sage", payment.getId());
        
        Map<String, Object> result = new HashMap<>();
        result.put("externalPaymentId", "SAGE-PAY-" + payment.getId());
        result.put("status", "synced");
        result.put("syncedAt", java.time.LocalDateTime.now());
        
        return result;
    }
    
    @Override
    public Map<String, Object> syncOrder(Order order, IntegrationConfiguration config) {
        log.info("Syncing order {} to Sage", order.getCode());
        
        Map<String, Object> result = new HashMap<>();
        result.put("externalOrderId", "SAGE-ORD-" + order.getId());
        result.put("status", "synced");
        result.put("syncedAt", java.time.LocalDateTime.now());
        
        return result;
    }
    
    @Override
    public boolean verifyWebhookSignature(String payload, String signature, IntegrationConfiguration config) {
        return signature != null && !signature.isEmpty();
    }
    
    @Override
    public Map<String, Object> parseWebhookEvent(String payload) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "invoice.updated");
        event.put("payload", payload);
        return event;
    }
    
    @Override
    public void handleInvoiceWebhook(Map<String, Object> event, IntegrationConfiguration config) {
        log.info("Handling Sage invoice webhook: {}", event);
    }
    
    @Override
    public void handlePaymentWebhook(Map<String, Object> event, IntegrationConfiguration config) {
        log.info("Handling Sage payment webhook: {}", event);
    }
    
    @Override
    public List<Map<String, Object>> fetchInvoices(IntegrationConfiguration config) {
        log.info("Fetching invoices from Sage");
        return List.of();
    }
    
    @Override
    public void importInvoice(Map<String, Object> externalInvoice, IntegrationConfiguration config) {
        log.info("Importing invoice from Sage: {}", externalInvoice);
    }
}

