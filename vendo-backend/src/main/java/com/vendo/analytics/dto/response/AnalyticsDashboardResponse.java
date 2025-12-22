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
public class AnalyticsDashboardResponse {
    
    private SalesAnalytics sales;
    private UserAnalytics users;
    private ProductAnalytics products;
    private List<Map<String, Object>> recentEvents;
}
