package com.vendo.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoReorderSettingsRequest {
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    private Long retailerId;
    private Long warehouseId;
    
    @NotNull(message = "Reorder point is required")
    @Positive(message = "Reorder point must be positive")
    private Integer reorderPoint;
    
    @NotNull(message = "Reorder quantity is required")
    @Positive(message = "Reorder quantity must be positive")
    private Integer reorderQuantity;
    
    private String reorderStrategy; // FIXED_QUANTITY, ECONOMIC_ORDER_QUANTITY
    private Boolean isEnabled;
}

