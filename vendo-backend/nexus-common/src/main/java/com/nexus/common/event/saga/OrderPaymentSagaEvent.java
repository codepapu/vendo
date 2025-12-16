package com.nexus.common.event.saga;

import com.nexus.common.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Saga event for coordinating order and payment operations.
 * Used in Saga pattern for distributed transaction management.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderPaymentSagaEvent extends BaseEvent {
    
    private String sagaId;
    private String step; // "create_order", "process_payment", "confirm_order", "compensate"
    private Long orderId;
    private Long paymentId;
    private Long userId;
    private BigDecimal amount;
    private String status; // "pending", "completed", "failed", "compensated"
    private String failureReason;
    
    public OrderPaymentSagaEvent(String sagaId, String step, Long orderId, 
                               Long paymentId, Long userId, BigDecimal amount, 
                               String status, String failureReason) {
        super("saga.order-payment", "order-service", userId);
        this.sagaId = sagaId;
        this.step = step;
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.userId = userId;
        this.amount = amount;
        this.status = status;
        this.failureReason = failureReason;
    }
}
