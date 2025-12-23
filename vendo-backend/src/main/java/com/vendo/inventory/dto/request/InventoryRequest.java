package com.vendo.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequest {
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    private Long retailerId;
    
    private Long warehouseId;
    
    @Min(value = 0, message = "Quantity must be non-negative")
    @Builder.Default
    private Integer quantity = 0;
    
    @Min(value = 0, message = "Reserved quantity must be non-negative")
    @Builder.Default
    private Integer reservedQuantity = 0;
    
    @Min(value = 0, message = "Low stock threshold must be non-negative")
    @Builder.Default
    private Integer lowStockThreshold = 10;
    
    @Min(value = 0, message = "Reorder point must be non-negative")
    @Builder.Default
    private Integer reorderPoint = 0;
}

