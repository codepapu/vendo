package com.vendo.product.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class B2BPricingTierRequest {
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @Size(max = 50, message = "Business type must not exceed 50 characters")
    private String businessType; // CORPORATE, SME, ENTERPRISE
    
    @NotNull(message = "Min quantity is required")
    @Min(value = 1, message = "Min quantity must be at least 1")
    private Integer minQuantity;
    
    private Integer maxQuantity; // Optional
    
    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private BigDecimal price;
    
    @DecimalMin(value = "0.0", message = "Discount percentage must be non-negative")
    @DecimalMax(value = "100.0", message = "Discount percentage cannot exceed 100")
    private BigDecimal discountPercentage;
    
    private Boolean isActive;
}

