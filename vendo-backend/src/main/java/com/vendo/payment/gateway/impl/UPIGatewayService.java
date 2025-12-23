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
 * Generic UPI Payment Gateway Implementation
 * Supports UPI payments through various UPI apps
 * Note: In production, would integrate with UPI payment gateway (Razorpay, Paytm, etc.)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UPIGatewayService implements PaymentGatewayService {
    
    @Value("${payment.upi.merchant-id:}")
    private String merchantId;
    
    @Value("${payment.upi.api-key:}")
    private String apiKey;
    
    @Value("${payment.upi.webhook-secret:}")
    private String webhookSecret;
    
    @Value("${payment.upi.provider:razorpay}")
    private String provider; // razorpay, paytm, etc.
    
    @Override
    public PaymentGatewayResponse processPayment(ProcessPaymentRequest request) {
        log.info("Processing payment via UPI for order: {}", request.getOrderId());
        
        try {
            // In production, would call UPI payment gateway API
            // UPI payments typically require VPA (Virtual Payment Address) or QR code
            
            String paymentId = "upi_" + System.currentTimeMillis();
            String transactionId = "txn_upi_" + System.currentTimeMillis();
            
            String upiId = request.getPaymentData() != null ? request.getPaymentData().get("upi_id") : null;
            String vpa = request.getPaymentData() != null ? request.getPaymentData().get("vpa") : null;
            
            Map<String, Object> gatewayData = new HashMap<>();
            gatewayData.put("upi_payment_id", paymentId);
            gatewayData.put("merchant_id", merchantId);
            gatewayData.put("payment_method", "UPI");
            gatewayData.put("upi_id", upiId);
            gatewayData.put("vpa", vpa);
            gatewayData.put("provider", provider);
            
            // UPI payments often require QR code or deep link
            if (vpa != null) {
                gatewayData.put("payment_type", "collect");
            } else {
                gatewayData.put("payment_type", "qr");
                gatewayData.put("qr_code", generateQRCode(paymentId, request.getAmount()));
            }
            
            return PaymentGatewayResponse.builder()
                    .success(true)
                    .chargeId(paymentId)
                    .transactionId(transactionId)
                    .status(PaymentStatus.PROCESSING)
                    .message("UPI payment initiated")
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .gatewayName(getGatewayName())
                    .gatewayData(gatewayData)
                    .build();
        } catch (Exception e) {
            log.error("UPI payment processing failed", e);
            return PaymentGatewayResponse.builder()
                    .success(false)
                    .status(PaymentStatus.FAILED)
                    .message("Payment processing failed: " + e.getMessage())
                    .errorCode("UPI_ERROR")
                    .errorMessage(e.getMessage())
                    .gatewayName(getGatewayName())
                    .build();
        }
    }
    
    @Override
    public PaymentGatewayResponse processRefund(String chargeId, Double amount, String reason) {
        log.info("Processing refund via UPI for transaction: {}", chargeId);
        
        try {
            // In production, would call UPI payment gateway refund API
            String refundId = "refund_upi_" + System.currentTimeMillis();
            
            Map<String, Object> gatewayData = new HashMap<>();
            gatewayData.put("refund_id", refundId);
            gatewayData.put("original_payment_id", chargeId);
            gatewayData.put("refund_reason", reason);
            gatewayData.put("provider", provider);
            
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
            log.error("UPI refund processing failed", e);
            return PaymentGatewayResponse.builder()
                    .success(false)
                    .status(PaymentStatus.FAILED)
                    .message("Refund processing failed: " + e.getMessage())
                    .errorCode("UPI_REFUND_ERROR")
                    .errorMessage(e.getMessage())
                    .gatewayName(getGatewayName())
                    .build();
        }
    }
    
    @Override
    public PaymentGatewayResponse getPaymentStatus(String chargeId) {
        log.info("Getting payment status from UPI gateway for transaction: {}", chargeId);
        
        try {
            // In production, would call UPI payment gateway status API
            return PaymentGatewayResponse.builder()
                    .success(true)
                    .chargeId(chargeId)
                    .status(PaymentStatus.COMPLETED)
                    .gatewayName(getGatewayName())
                    .build();
        } catch (Exception e) {
            log.error("Failed to get payment status from UPI gateway", e);
            return PaymentGatewayResponse.builder()
                    .success(false)
                    .status(PaymentStatus.FAILED)
                    .message("Failed to get payment status: " + e.getMessage())
                    .errorCode("UPI_STATUS_ERROR")
                    .errorMessage(e.getMessage())
                    .gatewayName(getGatewayName())
                    .build();
        }
    }
    
    @Override
    public boolean verifyWebhookSignature(String payload, String signature, String secret) {
        log.info("Verifying UPI webhook signature");
        
        if (secret == null || secret.isEmpty()) {
            log.warn("UPI webhook secret not configured");
            return false;
        }
        
        // In production, would verify using provider-specific signature algorithm
        return signature != null && !signature.isEmpty();
    }
    
    @Override
    public PaymentGatewayResponse handleWebhook(String payload, String signature) {
        log.info("Handling UPI webhook");
        
        if (!verifyWebhookSignature(payload, signature, webhookSecret)) {
            log.error("Invalid UPI webhook signature");
            return PaymentGatewayResponse.builder()
                    .success(false)
                    .message("Invalid webhook signature")
                    .errorCode("INVALID_SIGNATURE")
                    .gatewayName(getGatewayName())
                    .build();
        }
        
        try {
            // In production, would parse UPI webhook event
            // Handle different event types (payment.success, payment.failed, refund.success, etc.)
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
            log.error("Error processing UPI webhook", e);
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
        return "UPI";
    }
    
    private String generateQRCode(String paymentId, BigDecimal amount) {
        // In production, would generate actual UPI QR code
        // Format: upi://pay?pa=<merchant_vpa>&am=<amount>&cu=INR&tn=<transaction_note>
        return "upi://pay?pa=" + merchantId + "@paytm&am=" + amount + "&cu=INR&tn=Order_" + paymentId;
    }
    
    private String extractChargeIdFromPayload(String payload) {
        // Simplified - in production use proper JSON parsing
        if (payload.contains("payment_id") || payload.contains("transaction_id")) {
            return "upi_" + System.currentTimeMillis();
        }
        return null;
    }
    
    private PaymentStatus extractStatusFromPayload(String payload) {
        // Simplified - in production use proper JSON parsing
        if (payload.contains("success") || payload.contains("completed")) {
            return PaymentStatus.COMPLETED;
        } else if (payload.contains("failed") || payload.contains("error")) {
            return PaymentStatus.FAILED;
        } else if (payload.contains("refunded")) {
            return PaymentStatus.REFUNDED;
        }
        return PaymentStatus.PROCESSING;
    }
}

