package com.vendo.common.event.order;

import com.vendo.common.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Event published when an order is cancelled.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderCancelledEvent extends BaseEvent {
    
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private String reason;
    private Boolean refundRequired;
    
    public OrderCancelledEvent(Long orderId, String orderNumber, Long userId, 
                              String reason, Boolean refundRequired) {
        super("order.cancelled", "order-service", userId);
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.reason = reason;
        this.refundRequired = refundRequired;
    }
}
