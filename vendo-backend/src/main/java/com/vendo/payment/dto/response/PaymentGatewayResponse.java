package com.vendo.payment.dto.response;

import com.vendo.payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGatewayResponse {
    
    private boolean success;
    private String chargeId;
    private String transactionId;
    private PaymentStatus status;
    private String message;
    private BigDecimal amount;
    private String currency;
    private String gatewayName;
    
    // Gateway-specific response data
    private Map<String, Object> gatewayData;
    
    // For redirect-based payments (e.g., PayPal)
    private String redirectUrl;
    
    // Error details
    private String errorCode;
    private String errorMessage;
}
