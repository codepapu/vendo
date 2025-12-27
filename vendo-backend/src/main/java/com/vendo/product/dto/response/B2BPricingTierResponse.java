package com.vendo.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class B2BPricingTierResponse {
    
    private Long id;
    private Long productId;
    private String productName;
    private String businessType;
    private Integer minQuantity;
    private Integer maxQuantity;
    private BigDecimal price;
    private BigDecimal discountPercentage;
    private Boolean isActive;
    private LocalDateTime createdAt;
}

