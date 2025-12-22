package com.vendo.common.event.product;

import com.vendo.common.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Event published when product stock is updated.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductStockUpdatedEvent extends BaseEvent {
    
    private Long productId;
    private String sku;
    private Integer oldStock;
    private Integer newStock;
    private Integer quantityChange;
    private String reason; // "order", "restock", "adjustment", etc.
    private Long orderId; // If stock change is due to an order
    
    public ProductStockUpdatedEvent(Long productId, String sku, Integer oldStock, 
                                   Integer newStock, Integer quantityChange, 
                                   String reason, Long orderId) {
        super("product.stock.updated", "product-service", null);
        this.productId = productId;
        this.sku = sku;
        this.oldStock = oldStock;
        this.newStock = newStock;
        this.quantityChange = quantityChange;
        this.reason = reason;
        this.orderId = orderId;
    }
}
