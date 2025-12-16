package com.nexus.payment.dto.request;

import com.nexus.payment.enums.PaymentGateway;
import com.nexus.payment.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class ProcessPaymentRequest {
    
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Currency is required")
    @jakarta.validation.constraints.NotBlank(message = "Currency cannot be blank")
    @jakarta.validation.constraints.Size(min = 3, max = 3, message = "Currency must be 3 characters (ISO code)")
    private String currency;
    
    @NotNull(message = "Payment gateway is required")
    private PaymentGateway gateway;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    private String description;
    
    // Payment method specific data (e.g., card details, wallet info)
    private Map<String, String> paymentData;
    
    // Customer information
    private String customerEmail;
    private String customerName;
    private String customerPhone;
    
    // Return URLs
    private String returnUrl;
    private String cancelUrl;
    
    // Metadata
    private Map<String, String> metadata;
}
