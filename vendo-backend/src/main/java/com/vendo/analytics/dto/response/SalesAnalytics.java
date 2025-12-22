package com.vendo.analytics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesAnalytics {
    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private Long totalOrders;
    private Long todayOrders;
    private BigDecimal averageOrderValue;
    private List<Map<String, Object>> revenueByPeriod;
}
