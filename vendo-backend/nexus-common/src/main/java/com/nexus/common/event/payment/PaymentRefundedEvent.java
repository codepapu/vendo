package com.nexus.common.event.payment;

import com.nexus.common.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Event published when a payment is refunded.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentRefundedEvent extends BaseEvent {
    
    private Long paymentId;
    private Long refundId;
    private Long orderId;
    private Long userId;
    private BigDecimal refundAmount;
    private String currency;
    private String refundReason;
    private String gatewayRefundId;
    
    public PaymentRefundedEvent(Long paymentId, Long refundId, Long orderId, 
                               Long userId, BigDecimal refundAmount, 
                               String currency, String refundReason, 
                               String gatewayRefundId) {
        super("payment.refunded", "payment-service", userId);
        this.paymentId = paymentId;
        this.refundId = refundId;
        this.orderId = orderId;
        this.userId = userId;
        this.refundAmount = refundAmount;
        this.currency = currency;
        this.refundReason = refundReason;
        this.gatewayRefundId = gatewayRefundId;
    }
}
