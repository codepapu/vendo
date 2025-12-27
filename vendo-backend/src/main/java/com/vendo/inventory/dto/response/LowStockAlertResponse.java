package com.vendo.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockAlertResponse {
    
    private List<LowStockProduct> products;
    private Integer totalCount;
    private Integer warningCount;
    private Integer criticalCount;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LowStockProduct {
        private Long inventoryId;
        private Long productId;
        private String productName;
        private String sku;
        private Long retailerId;
        private String retailerName;
        private Long warehouseId;
        private String warehouseName;
        private Integer currentStock;
        private Integer availableQuantity;
        private Integer lowStockThreshold;
        private Integer reorderPoint;
        private String alertLevel; // WARNING, CRITICAL
    }
}

