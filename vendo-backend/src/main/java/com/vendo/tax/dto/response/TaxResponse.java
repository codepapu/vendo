package com.vendo.tax.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Tax Calculation Response DTO
 * Contains tax amount and detailed breakdown
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxResponse {
    
    private BigDecimal subTotal;
    private BigDecimal taxAmount;
    private BigDecimal total;
    
    private String currency;
    
    private List<TaxBreakdown> breakdown;
    
    private Boolean isExempt;
    private String exemptionReason;
    
    private String calculationMethod; // LOCATION, PRODUCT_TYPE, BUSINESS_TYPE, DEFAULT
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaxBreakdown {
        private String ruleName;
        private String ruleType;
        private BigDecimal rate;
        private BigDecimal amount;
        private String description;
    }
}

