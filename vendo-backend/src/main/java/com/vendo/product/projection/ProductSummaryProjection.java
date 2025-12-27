package com.vendo.product.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Projection interface for product summary data.
 * Reduces data transfer by only selecting essential fields for product listings.
 */
public interface ProductSummaryProjection {
    Long getId();
    String getName();
    String getDescription();
    String getStatus();
    String getImages();
    String getSku();
    Boolean getIsFeatured();
    Long getBrandId();
    String getBrandName();
    BigDecimal getPrice();
    BigDecimal getSalePrice();
    Double getAverageRating();
    Long getReviewCount();
    LocalDateTime getCreatedAt();
}

