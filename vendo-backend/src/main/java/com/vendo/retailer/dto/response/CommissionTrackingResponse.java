package com.vendo.retailer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionTrackingResponse {
    
    private Long id;
    private Long retailerId;
    private RetailerInfo retailer;
    private Long orderId;
    private Long orderItemId;
    private BigDecimal commissionAmount;
    private BigDecimal commissionRate;
    private String status;
    private LocalDate settlementDate;
    private String paymentReference;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetailerInfo {
        private Long id;
        private String storeName;
        private String storeSlug;
    }
}

