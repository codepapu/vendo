package com.vendo.tax.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Tax Rate Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxRateResponse {
    
    private String country;
    private String state;
    private String city;
    
    private String productType;
    private String businessType;
    
    private List<TaxRate> rates;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaxRate {
        private Long ruleId;
        private String ruleName;
        private String ruleType;
        private BigDecimal percentage;
        private Integer priority;
        private String description;
    }
}

