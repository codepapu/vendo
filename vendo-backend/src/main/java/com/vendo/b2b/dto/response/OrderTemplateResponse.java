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
public class OrderTemplateResponse {
    
    private Long id;
    private Long businessAccountId;
    private String businessAccountName;
    private String name;
    private String description;
    private Boolean isShared;
    private Long createdBy;
    private String createdByName;
    private Integer usageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderTemplateItemResponse> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderTemplateItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String sku;
        private Integer quantity;
        private java.math.BigDecimal unitPrice;
        private String notes;
        private Integer sortOrder;
    }
}

