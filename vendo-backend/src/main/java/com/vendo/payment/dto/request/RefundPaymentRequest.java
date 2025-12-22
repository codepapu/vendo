package com.vendo.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundPaymentRequest {
    
    @NotNull(message = "Payment ID is required")
    private Long paymentId;
    
    @Positive(message = "Refund amount must be positive")
    @jakarta.validation.constraints.DecimalMin(value = "0.01", message = "Refund amount must be at least 0.01")
    private BigDecimal amount; // If null, full refund
    
    private String reason;
    
    private String note;
}
