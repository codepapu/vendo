package com.vendo.common.event.product;

import com.vendo.common.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Event published when a new product is created.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductCreatedEvent extends BaseEvent {
    
    private Long productId;
    private String productName;
    private String sku;
    private BigDecimal price;
    private Long categoryId;
    private Integer stockQuantity;
    
    public ProductCreatedEvent(Long productId, String productName, String sku, 
                              BigDecimal price, Long categoryId, Integer stockQuantity) {
        super("product.created", "product-service", null);
        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.price = price;
        this.categoryId = categoryId;
        this.stockQuantity = stockQuantity;
    }
}
