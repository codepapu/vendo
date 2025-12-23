package com.vendo.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseRequest {
    
    @NotBlank(message = "Warehouse name is required")
    @Size(max = 255, message = "Warehouse name must not exceed 255 characters")
    private String name;
    
    @NotBlank(message = "Warehouse code is required")
    @Size(max = 50, message = "Warehouse code must not exceed 50 characters")
    private String code;
    
    private Long addressId;
    
    @Builder.Default
    private Boolean isActive = true;
}

