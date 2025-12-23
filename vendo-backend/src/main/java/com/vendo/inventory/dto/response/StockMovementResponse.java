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
public class StockMovementResponse {
    
    private Long id;
    private Long inventoryId;
    private InventoryInfo inventory;
    private String movementType;
    private Integer quantity;
    private String referenceType;
    private Long referenceId;
    private String notes;
    private Long createdBy;
    private UserInfo createdByUser;
    private LocalDateTime createdAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryInfo {
        private Long id;
        private Long productId;
        private String productName;
        private Long warehouseId;
        private String warehouseName;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
    }
}

