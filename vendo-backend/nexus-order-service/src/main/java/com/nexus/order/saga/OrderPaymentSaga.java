package com.nexus.order.saga;

import com.nexus.common.event.EventPublisher;
import com.nexus.common.event.saga.OrderPaymentSagaEvent;
import com.nexus.order.entity.Order;
import com.nexus.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Saga orchestrator for Order-Payment distributed transaction.
 * Implements Saga pattern for coordinating order creation and payment processing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentSaga {
    
    private final OrderRepository orderRepository;
    private final EventPublisher eventPublisher;
    
    /**
     * Start the order-payment saga.
     * 
     * @param orderId The order ID
     * @param paymentId The payment ID (if available)
     * @param userId The user ID
     * @param amount The payment amount
     * @return The saga ID
     */
    public String startSaga(Long orderId, Long paymentId, Long userId, java.math.BigDecimal amount) {
        String sagaId = UUID.randomUUID().toString();
        
        log.info("Starting Order-Payment Saga: sagaId={}, orderId={}, paymentId={}", 
                sagaId, orderId, paymentId);
        
        // Step 1: Create order (already done, just confirm)
        OrderPaymentSagaEvent createOrderEvent = new OrderPaymentSagaEvent(
                sagaId,
                "create_order",
                orderId,
                paymentId,
                userId,
                amount,
                "completed",
                null
        );
        eventPublisher.publish(createOrderEvent, "saga-order-payment");
        
        return sagaId;
    }
    
    /**
     * Process payment step in the saga.
     * 
     * @param sagaId The saga ID
     * @param orderId The order ID
     * @param paymentId The payment ID
     * @param userId The user ID
     * @param amount The payment amount
     * @param success Whether payment was successful
     * @param failureReason Reason for failure (if any)
     */
    public void processPayment(String sagaId, Long orderId, Long paymentId, 
                              Long userId, java.math.BigDecimal amount, 
                              boolean success, String failureReason) {
        log.info("Processing payment in saga: sagaId={}, orderId={}, paymentId={}, success={}", 
                sagaId, orderId, paymentId, success);
        
        OrderPaymentSagaEvent paymentEvent = new OrderPaymentSagaEvent(
                sagaId,
                "process_payment",
                orderId,
                paymentId,
                userId,
                amount,
                success ? "completed" : "failed",
                failureReason
        );
        eventPublisher.publish(paymentEvent, "saga-order-payment");
        
        if (success) {
            // Confirm order
            confirmOrder(sagaId, orderId, paymentId, userId, amount);
        } else {
            // Compensate (cancel order)
            compensate(sagaId, orderId, paymentId, userId, amount, failureReason);
        }
    }
    
    /**
     * Confirm order after successful payment.
     */
    private void confirmOrder(String sagaId, Long orderId, Long paymentId, 
                             Long userId, java.math.BigDecimal amount) {
        log.info("Confirming order in saga: sagaId={}, orderId={}", sagaId, orderId);
        
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
            
            order.setStatus("confirmed");
            order.setIsConfirmed(true);
            orderRepository.save(order);
            
            OrderPaymentSagaEvent confirmEvent = new OrderPaymentSagaEvent(
                    sagaId,
                    "confirm_order",
                    orderId,
                    paymentId,
                    userId,
                    amount,
                    "completed",
                    null
            );
            eventPublisher.publish(confirmEvent, "saga-order-payment");
            
        } catch (Exception e) {
            log.error("Error confirming order in saga: sagaId={}, orderId={}", sagaId, orderId, e);
            compensate(sagaId, orderId, paymentId, userId, amount, "Failed to confirm order: " + e.getMessage());
        }
    }
    
    /**
     * Compensate (rollback) the saga transaction.
     */
    private void compensate(String sagaId, Long orderId, Long paymentId, 
                           Long userId, java.math.BigDecimal amount, String reason) {
        log.warn("Compensating saga: sagaId={}, orderId={}, reason={}", sagaId, orderId, reason);
        
        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null && !"cancelled".equals(order.getStatus())) {
                order.setStatus("cancelled");
                order.setCancellationReason("Saga compensation: " + reason);
                orderRepository.save(order);
            }
            
            OrderPaymentSagaEvent compensateEvent = new OrderPaymentSagaEvent(
                    sagaId,
                    "compensate",
                    orderId,
                    paymentId,
                    userId,
                    amount,
                    "compensated",
                    reason
            );
            eventPublisher.publish(compensateEvent, "saga-order-payment");
            
        } catch (Exception e) {
            log.error("Error compensating saga: sagaId={}, orderId={}", sagaId, orderId, e);
        }
    }
}
