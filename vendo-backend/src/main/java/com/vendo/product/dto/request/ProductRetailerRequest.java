package com.vendo.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRetailerRequest {
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "Retailer ID is required")
    private Long retailerId;
    
    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be non-negative")
    private BigDecimal price;
    
    @Min(value = 0, message = "Compare at price must be non-negative")
    private BigDecimal compareAtPrice;
    
    @Min(value = 0, message = "Cost price must be non-negative")
    private BigDecimal costPrice;
    
    @Min(value = 0, message = "Stock quantity must be non-negative")
    @Builder.Default
    private Integer stockQuantity = 0;
    
    @Min(value = 0, message = "Low stock threshold must be non-negative")
    @Builder.Default
    private Integer lowStockThreshold = 10;
    
    @Builder.Default
    private Boolean isActive = true;
}

