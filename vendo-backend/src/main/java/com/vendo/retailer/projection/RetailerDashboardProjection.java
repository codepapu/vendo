package com.vendo.retailer.projection;

import java.math.BigDecimal;

/**
 * Projection interface for retailer dashboard aggregations.
 * Reduces data transfer by only selecting aggregated fields.
 */
public interface RetailerDashboardProjection {
    Long getRetailerId();
    String getStoreName();
    String getStatus();
    BigDecimal getTotalSales();
    Integer getTotalOrders();
    Integer getActiveProductsCount();
    Integer getLowStockProductsCount();
    BigDecimal getTotalCommissions();
    BigDecimal getPendingCommissions();
}

