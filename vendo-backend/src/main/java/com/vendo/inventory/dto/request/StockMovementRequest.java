package com.vendo.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementRequest {
    
    @NotNull(message = "Inventory ID is required")
    private Long inventoryId;
    
    @NotBlank(message = "Movement type is required")
    @Size(max = 50, message = "Movement type must not exceed 50 characters")
    private String movementType; // IN, OUT, ADJUSTMENT, RETURN
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    @Size(max = 50, message = "Reference type must not exceed 50 characters")
    private String referenceType; // ORDER, RETURN, ADJUSTMENT
    
    private Long referenceId;
    
    private String notes;
}

