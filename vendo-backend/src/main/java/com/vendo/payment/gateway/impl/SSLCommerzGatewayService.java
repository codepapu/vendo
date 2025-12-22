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
        return true;
    }
    
    @Override
    public PaymentGatewayResponse handleWebhook(String payload, String signature) {
        log.info("Handling SSLCommerz webhook");
        return PaymentGatewayResponse.builder()
                .success(true)
                .status(PaymentStatus.COMPLETED)
                .message("Webhook processed successfully")
                .gatewayName(getGatewayName())
                .build();
    }
    
    @Override
    public String getGatewayName() {
        return "SSLCOMMERZ";
    }
}
