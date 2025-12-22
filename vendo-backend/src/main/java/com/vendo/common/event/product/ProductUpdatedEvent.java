package com.vendo.common.event.product;

import com.vendo.common.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Event published when a product is updated.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductUpdatedEvent extends BaseEvent {
    
    private Long productId;
    private String productName;
    private String sku;
    private Map<String, Object> updatedFields;
    
    public ProductUpdatedEvent(Long productId, String productName, String sku, 
                              Map<String, Object> updatedFields) {
        super("product.updated", "product-service", null);
        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.updatedFields = updatedFields;
    }
}
