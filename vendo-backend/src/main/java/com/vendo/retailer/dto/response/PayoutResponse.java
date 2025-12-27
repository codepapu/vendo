package com.vendo.retailer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutResponse {
    
    private Long id;
    private Long retailerId;
    private String retailerName;
    private BigDecimal payoutAmount;
    private BigDecimal totalSales;
    private BigDecimal totalCommission;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private String status;
    private String payoutMethod;
    private String transactionId;
    private Long bankAccountId;
    private String notes;
    private LocalDateTime processedAt;
    private Long processedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

