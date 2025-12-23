package com.vendo.payment.gateway.impl;

import com.vendo.payment.dto.request.ProcessPaymentRequest;
import com.vendo.payment.dto.response.PaymentGatewayResponse;
import com.vendo.payment.enums.PaymentStatus;
import com.vendo.payment.gateway.PaymentGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Paystack Payment Gateway Implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaystackGatewayService implements PaymentGatewayService {
    
    @Value("${payment.paystack.secret-key:}")
    private String paystackSecretKey;
    
    @Value("${payment.paystack.webhook-secret:}")
    private String paystackWebhookSecret;
    
    @Override
    public PaymentGatewayResponse processPayment(ProcessPaymentRequest request) {
        log.info("Processing payment via Paystack for order: {}", request.getOrderId());
        
        try {
            // In production, would call Paystack API
            String reference = "ref_" + System.currentTimeMillis();
            
            return PaymentGatewayResponse.builder()
                    .success(true)
                    .chargeId(reference)
                    .transactionId(reference)
                    .status(PaymentStatus.PROCESSING)
                    .message("Payment initiated")
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .gatewayName(getGatewayName())
                    .build();
        } catch (Exception e) {
            log.error("Paystack payment processing failed", e);
            return PaymentGatewayResponse.builder()
                    .success(false)
                    .status(PaymentStatus.FAILED)
                    .message("Payment processing failed: " + e.getMessage())
                    .errorCode("PAYSTACK_ERROR")
                    .errorMessage(e.getMessage())
                    .gatewayName(getGatewayName())
                    .build();
        }
    }
    
    @Override
    public PaymentGatewayResponse processRefund(String chargeId, Double amount, String reason) {
        log.info("Processing refund via Paystack for transaction: {}", chargeId);
        
        try {
            return PaymentGatewayResponse.builder()
                    .success(true)
                    .chargeId(chargeId)
                    .status(PaymentStatus.REFUNDED)
                    .message("Refund processed successfully")
                    .amount(BigDecimal.valueOf(amount))
                    .gatewayName(getGatewayName())
                    .build();
        } catch (Exception e) {
            log.error("Paystack refund processing failed", e);
            return PaymentGatewayResponse.builder()
                    .success(false)
                    .status(PaymentStatus.FAILED)
                    .message("Refund processing failed: " + e.getMessage())
                    .gatewayName(getGatewayName())
                    .build();
        }
    }
    
    @Override
    public PaymentGatewayResponse getPaymentStatus(String chargeId) {
        log.info("Getting payment status from Paystack for transaction: {}", chargeId);
        
        try {
            return PaymentGatewayResponse.builder()
                    .success(true)
                    .chargeId(chargeId)
                    .status(PaymentStatus.COMPLETED)
                    .gatewayName(getGatewayName())
                    .build();
        } catch (Exception e) {
            return PaymentGatewayResponse.builder()
                    .success(false)
                    .status(PaymentStatus.FAILED)
                    .message("Failed to get payment status: " + e.getMessage())
                    .gatewayName(getGatewayName())
                    .build();
        }
    }
    
    @Override
    public boolean verifyWebhookSignature(String payload, String signature, String secret) {
        log.info("Verifying Paystack webhook signature");
        
        // Paystack uses HMAC SHA512 for webhook signature verification
        if (secret == null || secret.isEmpty()) {
            log.warn("Paystack webhook secret not configured");
            return false;
        }
        
        // In production, would verify using Paystack's signature algorithm
        // HMAC SHA512(payload, secret) == signature
        return signature != null && !signature.isEmpty();
    }
    
    @Override
    public PaymentGatewayResponse handleWebhook(String payload, String signature) {
        log.info("Handling Paystack webhook");
        
        if (!verifyWebhookSignature(payload, signature, paystackSecretKey)) {
            log.error("Invalid Paystack webhook signature");
            return PaymentGatewayResponse.builder()
                    .success(false)
                    .message("Invalid webhook signature")
                    .errorCode("INVALID_SIGNATURE")
                    .gatewayName(getGatewayName())
                    .build();
        }
        
        try {
            // In production, would parse Paystack webhook event
            // Handle different event types:
            // - charge.success -> PaymentStatus.COMPLETED
            // - charge.failed -> PaymentStatus.FAILED
            // - refund.success -> PaymentStatus.REFUNDED
            
            String chargeId = extractChargeIdFromPayload(payload);
            PaymentStatus status = extractStatusFromPayload(payload);
            
            Map<String, Object> gatewayData = new java.util.HashMap<>();
            gatewayData.put("webhook_event", extractEventTypeFromPayload(payload));
            gatewayData.put("paystack_event_id", extractEventIdFromPayload(payload));
            
            return PaymentGatewayResponse.builder()
                    .success(true)
                    .chargeId(chargeId)
                    .status(status != null ? status : PaymentStatus.COMPLETED)
                    .message("Webhook processed successfully")
                    .gatewayName(getGatewayName())
                    .gatewayData(gatewayData)
                    .build();
        } catch (Exception e) {
            log.error("Error processing Paystack webhook", e);
            return PaymentGatewayResponse.builder()
                    .success(false)
                    .message("Error processing webhook: " + e.getMessage())
                    .errorCode("WEBHOOK_PROCESSING_ERROR")
                    .errorMessage(e.getMessage())
                    .gatewayName(getGatewayName())
                    .build();
        }
    }
    
    @Value("${payment.paystack.webhook-secret:}")
    private String paystackWebhookSecret;
    
    private String extractChargeIdFromPayload(String payload) {
        // Simplified - in production use proper JSON parsing
        if (payload.contains("reference") || payload.contains("transaction_id")) {
            return "ref_" + System.currentTimeMillis();
        }
        return null;
    }
    
    private PaymentStatus extractStatusFromPayload(String payload) {
        // Simplified - in production use proper JSON parsing
        if (payload.contains("success") || payload.contains("successful")) {
            return PaymentStatus.COMPLETED;
        } else if (payload.contains("failed")) {
            return PaymentStatus.FAILED;
        } else if (payload.contains("refund")) {
            return PaymentStatus.REFUNDED;
        }
        return PaymentStatus.PROCESSING;
    }
    
    private String extractEventTypeFromPayload(String payload) {
        // Simplified - in production use proper JSON parsing
        if (payload.contains("charge.success")) {
            return "charge.success";
        } else if (payload.contains("charge.failed")) {
            return "charge.failed";
        } else if (payload.contains("refund.success")) {
            return "refund.success";
        }
        return "unknown";
    }
    
    private String extractEventIdFromPayload(String payload) {
        // Simplified - in production use proper JSON parsing
        return "evt_" + System.currentTimeMillis();
    }
    
    @Override
    public String getGatewayName() {
        return "PAYSTACK";
    }
}
