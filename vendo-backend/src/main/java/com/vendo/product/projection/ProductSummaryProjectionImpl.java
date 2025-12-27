package com.vendo.product.projection;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Class-based projection implementation for product summary data.
 * Used with JPQL constructor expressions to reduce data transfer.
 * Constructor parameters must match the order in the repository query.
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductSummaryProjectionImpl implements ProductSummaryProjection {
    private Long id;
    private String name;
    private String description;
    private String status;
    private String images;
    private String sku;
    private Boolean isFeatured;
    private Long brandId;
    private String brandName;
    private BigDecimal price;
    private BigDecimal salePrice;
    private Double averageRating;
    private Long reviewCount;
    private LocalDateTime createdAt;
    
    /**
     * Constructor matching the JPQL query order:
     * p.id, p.name, p.description, p.status, p.images, p.sku, p.isFeatured,
     * b.id, b.name, p.price, p.salePrice, averageRating, reviewCount, p.createdAt
     */
    public ProductSummaryProjectionImpl(Long id, String name, String description, String status, 
                                        String images, String sku, Boolean isFeatured,
                                        Long brandId, String brandName, BigDecimal price, BigDecimal salePrice,
                                        Double averageRating, Long reviewCount, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.images = images;
        this.sku = sku;
        this.isFeatured = isFeatured;
        this.brandId = brandId;
        this.brandName = brandName;
        this.price = price;
        this.salePrice = salePrice;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
        this.createdAt = createdAt;
    }
}
