package com.vendo.common.event.order;

import com.vendo.common.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Event published when an order status is updated.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderUpdatedEvent extends BaseEvent {
    
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private String oldStatus;
    private String newStatus;
    private String reason;
    
    public OrderUpdatedEvent(Long orderId, String orderNumber, Long userId, 
                            String oldStatus, String newStatus, String reason) {
        super("order.updated", "order-service", userId);
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.reason = reason;
    }
}
