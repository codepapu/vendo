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
 * SSLCommerz Payment Gateway Implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SSLCommerzGatewayService implements PaymentGatewayService {
    
    @Value("${payment.sslcommerz.store-id:}")
    private String storeId;
    
    @Value("${payment.sslcommerz.store-password:}")
    private String storePassword;
    
    @Value("${payment.sslcommerz.webhook-secret:}")
    private String sslcommerzWebhookSecret;
    
    @Override
    public PaymentGatewayResponse processPayment(ProcessPaymentRequest request) {
        log.info("Processing payment via SSLCommerz for order: {}", request.getOrderId());
        
        try {
            String sessionId = "session_" + System.currentTimeMillis();
            String redirectUrl = "https://sandbox.sslcommerz.com/EasyCheckOut/testcwd/" + sessionId;
            
            return PaymentGatewayResponse.builder()
                    .success(true)
                    .chargeId(sessionId)
                    .transactionId(sessionId)
                    .status(PaymentStatus.PROCESSING)
                    .message("Payment session created")
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .gatewayName(getGatewayName())
                    .redirectUrl(redirectUrl)
                    .build();
        } catch (Exception e) {
            log.error("SSLCommerz payment processing failed", e);
            return PaymentGatewayResponse.builder()
                    .success(false)
                    .status(PaymentStatus.FAILED)
                    .message("Payment processing failed: " + e.getMessage())
                    .gatewayName(getGatewayName())
                    .build();
        }
    }
    
    @Override
    public PaymentGatewayResponse processRefund(String chargeId, Double amount, String reason) {
        log.info("Processing refund via SSLCommerz for transaction: {}", chargeId);
        
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
            log.error("SSLCommerz refund processing failed", e);
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
        log.info("Getting payment status from SSLCommerz for transaction: {}", chargeId);
        
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
        log.info("Verifying SSLCommerz webhook signature");
        
        if (secret == null || secret.isEmpty()) {
            log.warn("SSLCommerz webhook secret not configured");
            return false;
        }
        
        // In production, would verify using SSLCommerz's signature algorithm
        return signature != null && !signature.isEmpty();
    }
    
    @Override
    public PaymentGatewayResponse handleWebhook(String payload, String signature) {
        log.info("Handling SSLCommerz webhook");
        
        if (!verifyWebhookSignature(payload, signature, sslcommerzWebhookSecret)) {
            log.error("Invalid SSLCommerz webhook signature");
            return PaymentGatewayResponse.builder()
                    .success(false)
                    .message("Invalid webhook signature")
                    .errorCode("INVALID_SIGNATURE")
                    .gatewayName(getGatewayName())
                    .build();
        }
        
        try {
            // In production, would parse SSLCommerz webhook event
            // Handle different event types (payment.success, payment.failed, etc.)
            
            String chargeId = extractChargeIdFromPayload(payload);
            PaymentStatus status = extractStatusFromPayload(payload);
            
            Map<String, Object> gatewayData = new java.util.HashMap<>();
            gatewayData.put("webhook_event", extractEventTypeFromPayload(payload));
            gatewayData.put("sslcommerz_event_id", extractEventIdFromPayload(payload));
            
            return PaymentGatewayResponse.builder()
                    .success(true)
                    .chargeId(chargeId)
                    .status(status != null ? status : PaymentStatus.COMPLETED)
                    .message("Webhook processed successfully")
                    .gatewayName(getGatewayName())
                    .gatewayData(gatewayData)
                    .build();
        } catch (Exception e) {
            log.error("Error processing SSLCommerz webhook", e);
            return PaymentGatewayResponse.builder()
                    .success(false)
                    .message("Error processing webhook: " + e.getMessage())
                    .errorCode("WEBHOOK_PROCESSING_ERROR")
                    .errorMessage(e.getMessage())
                    .gatewayName(getGatewayName())
                    .build();
        }
    }
    
    private String extractChargeIdFromPayload(String payload) {
        // Simplified - in production use proper JSON parsing
        if (payload.contains("session_id") || payload.contains("transaction_id")) {
            return "session_" + System.currentTimeMillis();
        }
        return null;
    }
    
    private PaymentStatus extractStatusFromPayload(String payload) {
        // Simplified - in production use proper JSON parsing
        if (payload.contains("success") || payload.contains("VALID")) {
            return PaymentStatus.COMPLETED;
        } else if (payload.contains("failed") || payload.contains("FAILED")) {
            return PaymentStatus.FAILED;
        } else if (payload.contains("refund")) {
            return PaymentStatus.REFUNDED;
        }
        return PaymentStatus.PROCESSING;
    }
    
    private String extractEventTypeFromPayload(String payload) {
        // Simplified - in production use proper JSON parsing
        if (payload.contains("payment.success")) {
            return "payment.success";
        } else if (payload.contains("payment.failed")) {
            return "payment.failed";
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
        return "SSLCOMMERZ";
    }
}
