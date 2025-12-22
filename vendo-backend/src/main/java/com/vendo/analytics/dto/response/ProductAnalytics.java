package com.vendo.analytics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAnalytics {
    private Long totalProducts;
    private Long topSellingProducts;
    private List<Map<String, Object>> productViews;
    private List<Map<String, Object>> productSales;
}
