package com.vendo.product.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Class-based projection implementation for basic product information.
 * Used with JPQL constructor expressions for lightweight data transfer.
 * Constructor parameters must match the order in the repository query.
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductBasicInfoProjectionImpl implements ProductBasicInfoProjection {
    private Long id;
    private String name;
    private String sku;
    private BigDecimal price;
    private BigDecimal salePrice;
    private String status;
    private Boolean isFeatured;
    
    /**
     * Constructor matching the JPQL query order:
     * p.id, p.name, p.sku, p.price, p.salePrice, p.status, p.isFeatured
     */
    public ProductBasicInfoProjectionImpl(Long id, String name, String sku, BigDecimal price, 
                                         BigDecimal salePrice, String status, Boolean isFeatured) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.salePrice = salePrice;
        this.status = status;
        this.isFeatured = isFeatured;
    }
}

