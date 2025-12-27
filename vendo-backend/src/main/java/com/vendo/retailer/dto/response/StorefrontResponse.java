package com.vendo.retailer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorefrontResponse {
    
    private Long retailerId;
    private String storeName;
    private String storeSlug;
    private String description;
    private String logoUrl;
    private String bannerUrl;
    private BigDecimal rating;
    private BigDecimal totalSales;
    private Integer totalOrders;
    private Integer productCount;
    private Integer reviewCount;
    private Boolean verified;
}

