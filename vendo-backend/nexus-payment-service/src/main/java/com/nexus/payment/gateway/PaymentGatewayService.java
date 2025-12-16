package com.nexus.payment.gateway;

import com.nexus.payment.dto.request.ProcessPaymentRequest;
import com.nexus.payment.dto.response.PaymentGatewayResponse;

/**
 * Payment Gateway Service Interface
 * All payment gateway implementations must implement this interface
 */
public interface PaymentGatewayService {
    
    /**
     * Process a payment
     */
    PaymentGatewayResponse processPayment(ProcessPaymentRequest request);
    
    /**
     * Process a refund
     */
    PaymentGatewayResponse processRefund(String chargeId, Double amount, String reason);
    
    /**
     * Get payment status
     */
    PaymentGatewayResponse getPaymentStatus(String chargeId);
    
    /**
     * Verify webhook signature
     */
    boolean verifyWebhookSignature(String payload, String signature, String secret);
    
    /**
     * Handle webhook event
     */
    PaymentGatewayResponse handleWebhook(String payload, String signature);
    
    /**
     * Get gateway name
     */
    String getGatewayName();
}
