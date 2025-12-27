package com.vendo.b2b.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkOrderResponse {
    
    private Long id;
    private Long businessAccountId;
    private String businessAccountName;
    private String name;
    private String description;
    private String sourceFileName;
    private String sourceFileType;
    private Integer totalItems;
    private Integer processedItems;
    private Integer failedItems;
    private String status;
    private String validationErrors;
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<BulkOrderItemResponse> items;
    private List<Long> createdOrderIds; // IDs of orders created from bulk import
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkOrderItemResponse {
        private Long id;
        private Integer rowNumber;
        private Long productId;
        private String productSku;
        private String productName;
        private Integer quantity;
        private java.math.BigDecimal unitPrice;
        private java.math.BigDecimal totalPrice;
        private Long orderId;
        private String orderNumber;
        private String status;
        private String validationErrors;
        private String processingErrors;
    }
}

