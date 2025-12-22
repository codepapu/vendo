package com.vendo.common.event.payment;

import com.vendo.common.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Event published when a payment fails.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentFailedEvent extends BaseEvent {
    
    private Long paymentId;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String failureReason;
    private String errorCode;
    
    public PaymentFailedEvent(Long paymentId, Long orderId, Long userId, 
                             BigDecimal amount, String currency, 
                             String paymentMethod, String failureReason, 
                             String errorCode) {
        super("payment.failed", "payment-service", userId);
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.failureReason = failureReason;
        this.errorCode = errorCode;
    }
}
