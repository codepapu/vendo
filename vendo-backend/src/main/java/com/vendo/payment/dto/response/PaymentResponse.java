package com.vendo.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Payment Response DTO
 * Contains all transaction details including gateway-specific data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    
    private Long id;
    private String chargeId;
    private String transactionId;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private BigDecimal paymentFee;
    private BigDecimal refundedAmount;
    private String currency;
    private String paymentChannel;
    private String paymentGateway;
    private String paymentMethod;
    private String status;
    private String paymentType;
    private String description;
    private String refundNote;
    private String metadata;
    
    // Gateway-specific transaction details
    private Map<String, Object> gatewayData;
    
    // Redirect URL for redirect-based payments (PayPal, etc.)
    private String redirectUrl;
    
    // Error details if payment failed
    private String errorCode;
    private String errorMessage;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
