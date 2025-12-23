package com.vendo.inventory.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInventoryRequest {
    
    @Min(value = 0, message = "Quantity must be non-negative")
    private Integer quantity;
    
    @Min(value = 0, message = "Reserved quantity must be non-negative")
    private Integer reservedQuantity;
    
    @Min(value = 0, message = "Low stock threshold must be non-negative")
    private Integer lowStockThreshold;
    
    @Min(value = 0, message = "Reorder point must be non-negative")
    private Integer reorderPoint;
}

