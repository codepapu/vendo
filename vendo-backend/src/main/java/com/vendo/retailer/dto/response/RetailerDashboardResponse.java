package com.vendo.retailer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetailerDashboardResponse {
    
    private Long retailerId;
    private String storeName;
    private String status;
    private BigDecimal totalSales;
    private Integer totalOrders;
    private Integer activeProducts;
    private Integer lowStockProducts;
    private BigDecimal totalCommissions;
    private BigDecimal pendingCommissions;
    private BigDecimal thisMonthSales;
    private Integer thisMonthOrders;
    private List<RecentOrder> recentOrders;
    private List<TopProduct> topProducts;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentOrder {
        private Long orderId;
        private String orderNumber;
        private BigDecimal totalAmount;
        private String status;
        private String createdAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProduct {
        private Long productId;
        private String productName;
        private Integer quantitySold;
        private BigDecimal totalRevenue;
    }
}

