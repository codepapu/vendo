package com.vendo.tax.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Tax Calculation Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxRequest {
    
    private BigDecimal subTotal;
    
    private String country;
    private String state;
    private String city;
    private String zipCode;
    
    private String businessType; // CORPORATE, SME, ENTERPRISE, B2C
    private Long businessAccountId;
    private Long userId;
    
    private List<TaxItemRequest> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaxItemRequest {
        private Long productId;
        private String productType; // SIMPLE, VARIABLE, GROUPED, DIGITAL, PHYSICAL
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal total;
    }
}

