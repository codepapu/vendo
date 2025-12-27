package com.vendo.product.projection;

import java.math.BigDecimal;

/**
 * Projection interface for basic product information.
 * Used for lightweight queries that only need minimal product data.
 */
public interface ProductBasicInfoProjection {
    Long getId();
    String getName();
    String getSku();
    BigDecimal getPrice();
    BigDecimal getSalePrice();
    String getStatus();
    Boolean getIsFeatured();
}

