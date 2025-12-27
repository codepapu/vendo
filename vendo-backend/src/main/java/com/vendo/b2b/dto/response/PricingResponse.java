package com.vendo.b2b.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingResponse {
    
    private Long productId;
    private Long businessAccountId;
    private String businessType;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal originalPrice;
    private BigDecimal discountAmount;
    private BigDecimal discountPercentage;
    private BigDecimal totalPrice;
    private String pricingTierName; // Optional: name of the pricing tier applied
}

