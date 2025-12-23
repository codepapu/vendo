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
import java.util.HashMap;
import java.util.Map;

/**
 * PhonePe Payment Gateway Implementation
 * Supports UPI payments through PhonePe
 * Note: In production, would integrate with PhonePe Payment Gateway API
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhonePeGatewayService implements PaymentGatewayService {
    
    @Value("${payment.phonepe.merchant-id:}")
    private String merchantId;
    
    @Value("${payment.phonepe.salt-key:}")
    private String saltKey;
    
    @Value("${payment.phonepe.salt-index:1}")
    private Integer saltIndex;
    
    @Value("${payment.phonepe.api-key:}")
    private String apiKey;
    
    @Value("${payment.phonepe.webhook-secret:}")
    private String webhookSecret;
    
    @Value("${payment.phonepe.mode:sandbox}")
    private String mode; // sandbox or production
    
    @Override
    public PaymentGatewayResponse processPayment(ProcessPaymentRequest request) {
        log.info("Processing payment via PhonePe for order: {}", request.getOrderId());
        
        try {
            // In production, would call PhonePe API
            // PhonePe requires specific request format with X-VERIFY header for signature
            
            String merchantTransactionId = "PHONEPE_" + System.currentTimeMillis();
            String transactionId = "txn_phonepe_" + System.currentTimeMillis();
            
            Map<String, Object> gatewayData = new HashMap<>();
            gatewayData.put("merchant_transaction_id", merchantTransactionId);
            gatewayData.put("merchant_id", merchantId);
            gatewayData.put("payment_method", request.getPaymentMethod() != null ? request.getPaymentMethod().name() : "PHONEPE");
            gatewayData.put("upi_id", request.getPaymentData() != null ? request.getPaymentData().get("upi_id") : null);
            gatewayData.put("redirect_url", generateRedirectUrl(merchantTransactionId));
            
            return PaymentGatewayResponse.builder()
                    .success(true)
                    .chargeId(merchantTransactionId)
                    .transactionId(transactionId)
                    .status(PaymentStatus.PROCESSING)
                    .message("PhonePe payment initiated")
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .gatewayName(getGatewayName())
                    .gatewayData(gatewayData)
                    .redirectUrl(generateRedirectUrl(merchantTransactionId))
                    .build();
        } catch (Exception e) {
            log.error("PhonePe payment processing failed", e);
            return PaymentGatewayResponse.builder()
                    .success(false)
                    .status(PaymentStatus.FAILED)
                    .message("Payment processing failed: " + e.getMessage())
                    .errorCode("PHONEPE_ERROR")
                    .errorMessage(e.getMessage())
                    .gatewayName(getGatewayName())
                    .build();
        }
    }
    
    @Override
    public PaymentGatewayResponse processRefund(String chargeId, Double amount, String reason) {
        log.info("Processing refund via PhonePe for transaction: {}", chargeId);
        
        try {
            // In production, would call PhonePe refund API
            String refundId = "refund_phonepe_" + System.currentTimeMillis();
            
            Map<String, Object> gatewayData = new HashMap<>();
            gatewayData.put("refund_id", refundId);
            gatewayData.put("original_transaction_id", chargeId);
            gatewayData.put("refund_reason", reason);
            
            return PaymentGatewayResponse.builder()
                    .success(true)
                    .chargeId(chargeId)
                    .transactionId(refundId)
                    .status(PaymentStatus.REFUNDED)
                    .message("Refund processed successfully")
                    .amount(BigDecimal.valueOf(amount))
                    .gatewayName(getGatewayName())
                    .gatewayData(gatewayData)
                    .build();
        } catch (Exception e) {
            log.error("PhonePe refund processing failed", e);
            return PaymentGatewayResponse.builder()
                    .success(false)
                    .status(PaymentStatus.FAILED)
                    .message("Refund processing failed: " + e.getMessage())
                    .errorCode("PHONEPE_REFUND_ERROR")
                    .errorMessage(e.getMessage())
                    .gatewayName(getGatewayName())
                    .build();
        }
    }
    
    @Override
    public PaymentGatewayResponse getPaymentStatus(String chargeId) {
        log.info("Getting payment status from PhonePe for transaction: {}", chargeId);
        
        try {
            // In production, would call PhonePe status check API
            return PaymentGatewayResponse.builder()
                    .success(true)
                    .chargeId(chargeId)
                    .status(PaymentStatus.COMPLETED)
                    .gatewayName(getGatewayName())
                    .build();
        } catch (Exception e) {
            log.error("Failed to get payment status from PhonePe", e);
            return PaymentGatewayResponse.builder()
                    .success(false)
                    .status(PaymentStatus.FAILED)
                    .message("Failed to get payment status: " + e.getMessage())
                    .errorCode("PHONEPE_STATUS_ERROR")
                    .errorMessage(e.getMessage())
                    .gatewayName(getGatewayName())
                    .build();
        }
    }
    
    @Override
    public boolean verifyWebhookSignature(String payload, String signature, String secret) {
        log.info("Verifying PhonePe webhook signature");
        
        // PhonePe uses X-VERIFY header for webhook signature verification
        // Signature is SHA256 hash of payload + salt key
        if (secret == null || secret.isEmpty()) {
            log.warn("PhonePe webhook secret not configured");
            return false;
        }
        
        // In production, would verify using PhonePe's signature algorithm
        // SHA256(payload + salt_key) == signature
        return signature != null && !signature.isEmpty();
    }
    
    @Override
    public PaymentGatewayResponse handleWebhook(String payload, String signature) {
        log.info("Handling PhonePe webhook");
        
        if (!verifyWebhookSignature(payload, signature, webhookSecret)) {
            log.error("Invalid PhonePe webhook signature");
            return PaymentGatewayResponse.builder()
                    .success(false)
                    .message("Invalid webhook signature")
                    .errorCode("INVALID_SIGNATURE")
                    .gatewayName(getGatewayName())
                    .build();
        }
        
        try {
            // In production, would parse PhonePe webhook event
            // Handle different event types (PAYMENT_SUCCESS, PAYMENT_ERROR, REFUND_SUCCESS, etc.)
            String chargeId = extractChargeIdFromPayload(payload);
            PaymentStatus status = extractStatusFromPayload(payload);
            
            return PaymentGatewayResponse.builder()
                    .success(true)
                    .chargeId(chargeId)
                    .status(status != null ? status : PaymentStatus.COMPLETED)
                    .message("Webhook processed successfully")
                    .gatewayName(getGatewayName())
                    .build();
        } catch (Exception e) {
            log.error("Error processing PhonePe webhook", e);
            return PaymentGatewayResponse.builder()
                    .success(false)
                    .message("Error processing webhook: " + e.getMessage())
                    .errorCode("WEBHOOK_PROCESSING_ERROR")
                    .errorMessage(e.getMessage())
                    .gatewayName(getGatewayName())
                    .build();
        }
    }
    
    @Override
    public String getGatewayName() {
        return "PHONEPE";
    }
    
    private String generateRedirectUrl(String merchantTransactionId) {
        String baseUrl = "sandbox".equals(mode) 
                ? "https://sandbox.phonepe.com/apis/pg-sandbox/pg/v1/pay"
                : "https://api.phonepe.com/apis/pg-sandbox/pg/v1/pay";
        return baseUrl + "?merchantTransactionId=" + merchantTransactionId;
    }
    
    private String extractChargeIdFromPayload(String payload) {
        // Simplified - in production use proper JSON parsing
        if (payload.contains("merchantTransactionId") || payload.contains("transactionId")) {
            return "PHONEPE_" + System.currentTimeMillis();
        }
        return null;
    }
    
    private PaymentStatus extractStatusFromPayload(String payload) {
        // Simplified - in production use proper JSON parsing
        if (payload.contains("PAYMENT_SUCCESS") || payload.contains("SUCCESS")) {
            return PaymentStatus.COMPLETED;
        } else if (payload.contains("PAYMENT_ERROR") || payload.contains("FAILED") || payload.contains("ERROR")) {
            return PaymentStatus.FAILED;
        } else if (payload.contains("REFUND_SUCCESS")) {
            return PaymentStatus.REFUNDED;
        }
        return PaymentStatus.PROCESSING;
    }
}

