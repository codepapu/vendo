package com.vendo.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentRequest {
    
    @NotNull(message = "Inventory ID is required")
    private Long inventoryId;
    
    @NotNull(message = "Quantity adjustment is required")
    private Integer quantity; // Positive for increase, negative for decrease
    
    @NotNull(message = "Reason is required")
    private String reason;
    
    private Integer adjustmentThreshold; // Override default threshold
}

