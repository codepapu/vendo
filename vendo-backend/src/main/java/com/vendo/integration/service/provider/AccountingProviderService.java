package com.vendo.integration.service.provider;

import com.vendo.integration.entity.IntegrationConfiguration;
import com.vendo.order.entity.Invoice;
import com.vendo.order.entity.Order;
import com.vendo.payment.entity.Payment;

import java.util.List;
import java.util.Map;

public interface AccountingProviderService {
    
    /**
     * Sync invoice to accounting system
     */
    Map<String, Object> syncInvoice(Invoice invoice, IntegrationConfiguration config);
    
    /**
     * Sync payment to accounting system
     */
    Map<String, Object> syncPayment(Payment payment, IntegrationConfiguration config);
    
    /**
     * Sync order to accounting system
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
     * Handle invoice webhook event
     */
    void handleInvoiceWebhook(Map<String, Object> event, IntegrationConfiguration config);
    
    /**
     * Handle payment webhook event
     */
    void handlePaymentWebhook(Map<String, Object> event, IntegrationConfiguration config);
    
    /**
     * Fetch invoices from accounting system (inbound sync)
     */
    List<Map<String, Object>> fetchInvoices(IntegrationConfiguration config);
    
    /**
     * Import invoice from accounting system
     */
    void importInvoice(Map<String, Object> externalInvoice, IntegrationConfiguration config);
}

