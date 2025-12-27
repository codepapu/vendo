package com.vendo.b2b.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceListResponse {
    
    private Long id;
    private Long businessAccountId;
    private String businessAccountName;
    private String name;
    private String description;
    private Integer version;
    private LocalDate effectiveFrom;
    private LocalDate effectiveUntil;
    private Boolean isActive;
    private Boolean isDefault;
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PriceListItemResponse> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceListItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productSku;
        private java.math.BigDecimal price;
        private Integer minQuantity;
        private Integer maxQuantity;
        private java.math.BigDecimal discountPercentage;
        private Boolean isActive;
    }
}

