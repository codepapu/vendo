package com.vendo.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoReorderSettingsResponse {
    
    private Long id;
    private Long productId;
    private String productName;
    private Long retailerId;
    private String retailerName;
    private Long warehouseId;
    private String warehouseName;
    private Integer reorderPoint;
    private Integer reorderQuantity;
    private String reorderStrategy;
    private Boolean isEnabled;
    private LocalDateTime lastReorderAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

