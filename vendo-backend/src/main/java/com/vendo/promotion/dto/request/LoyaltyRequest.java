package com.vendo.promotion.dto.request;

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
public class LoyaltyRequest {
    
    @NotNull(message = "Purchase amount is required")
    @Positive(message = "Purchase amount must be positive")
    private BigDecimal purchaseAmount;
}

