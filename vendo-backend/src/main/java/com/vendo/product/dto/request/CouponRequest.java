package com.vendo.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CouponRequest {
    
    @NotBlank(message = "Coupon code is required")
    @Size(max = 50, message = "Coupon code must not exceed 50 characters")
    private String code;
    
    @NotBlank(message = "Coupon name is required")
    @Size(max = 255, message = "Coupon name must not exceed 255 characters")
    private String name;
    
    private String description;
    
    @NotBlank(message = "Discount type is required")
    @Size(max = 50, message = "Discount type must not exceed 50 characters")
    private String discountType; // PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING
    
    @NotNull(message = "Discount value is required")
    @Min(value = 0, message = "Discount value must be greater than or equal to 0")
    private BigDecimal discountValue;
    
    @Min(value = 0, message = "Minimum purchase must be greater than or equal to 0")
    private BigDecimal minimumPurchase;
    
    @Min(value = 0, message = "Maximum discount must be greater than or equal to 0")
    private BigDecimal maximumDiscount;
    
    @Min(value = 1, message = "Usage limit must be greater than 0")
    private Integer usageLimit;
    
    @NotNull(message = "Valid from date is required")
    private LocalDateTime validFrom;
    
    @NotNull(message = "Valid until date is required")
    private LocalDateTime validUntil;
    
    @Size(max = 50, message = "Applicable to must not exceed 50 characters")
    private String applicableTo; // ALL, B2C, B2B, SPECIFIC_PRODUCTS, SPECIFIC_CATEGORIES
    
    private Boolean isActive;
    
    private List<Long> productIds;
    
    private List<Long> categoryIds;
}
