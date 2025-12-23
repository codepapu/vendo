package com.vendo.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponResponse {
    
    private Long id;
    private String code;
    private String name;
    private String description;
    private String discountType; // PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING
    private BigDecimal discountValue;
    private BigDecimal minimumPurchase;
    private BigDecimal maximumDiscount;
    private Integer usageLimit;
    private Integer usedCount;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private String applicableTo; // ALL, B2C, B2B, SPECIFIC_PRODUCTS, SPECIFIC_CATEGORIES
    private Boolean isActive;
    private LocalDateTime createdAt;
    private List<Long> productIds;
    private List<Long> categoryIds;
    
    // Discount calculation details (for applyCoupon response)
    private BigDecimal calculatedDiscount;
    private BigDecimal originalAmount;
    private BigDecimal finalAmount;
    private Boolean isValid;
    private String validationMessage;
}
