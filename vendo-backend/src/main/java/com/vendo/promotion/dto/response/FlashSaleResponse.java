package com.vendo.promotion.dto.response;

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
public class FlashSaleResponse {
    
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal salePrice;
    private BigDecimal originalPrice;
    private BigDecimal discountPercentage;
    private Integer quantityLimit;
    private Integer quantitySold;
    private Integer availableQuantity;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isActive;
}

