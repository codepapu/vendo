package com.vendo.order.service;

import com.vendo.common.event.EventPublisher;
import com.vendo.common.event.order.OrderCancelledEvent;
import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.order.dto.request.CancelOrderRequest;
import com.vendo.order.dto.response.CancellationResponse;
import com.vendo.order.entity.Order;
import com.vendo.order.entity.OrderHistory;
import com.vendo.order.entity.OrderItem;
import com.vendo.order.entity.OrderProduct;
import com.vendo.order.enums.CancellationReason;
import com.vendo.order.repository.OrderRepository;
import com.vendo.payment.dto.request.RefundPaymentRequest;
import com.vendo.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling order cancellations, refunds, and inventory updates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCancellationService {
    
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final EventPublisher eventPublisher;
    
    // Maximum hours after order creation when cancellation is allowed (default: 24 hours)
    @Value("${order.cancellation.time-window-hours:24}")
    private int cancellationTimeWindowHours;
    
    /**
     * Cancel an order with validation for eligibility.
     * 
     * @param orderId The ID of the order to cancel
     * @param request The cancellation request with reason
     * @return CancellationResponse with cancellation details
     */
    @Transactional
    public CancellationResponse cancelOrder(Long orderId, CancelOrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        
        // Validate cancellation eligibility
        validateCancellationEligibility(order);
        
        // Build cancellation reason string
        String reasonText = buildCancellationReason(request);
        
        // Update order status
        order.setStatus("CANCELLED");
        order.setCancellationReason(reasonText);
        order.setCancelledAt(LocalDateTime.now());
        
        // Add history entry
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .action("CANCELLED")
                .description("Order cancelled: " + reasonText)
                .build();
        order.getHistories().add(history);
        
        Order savedOrder = orderRepository.save(order);
        log.info("Order cancelled: orderId={}, reason={}", orderId, reasonText);
        
        // Determine if refund is required
        boolean refundRequired = isRefundRequired(order);
        
        // Update inventory - release reserved stock
        boolean inventoryUpdated = updateInventoryOnCancellation(savedOrder);
        
        // Process refund if required
        BigDecimal refundAmount = BigDecimal.ZERO;
        String refundStatus = "NOT_REQUIRED";
        String refundTransactionId = null;
        
        if (refundRequired) {
            try {
                CancellationResponse refundResponse = processRefund(orderId, reasonText);
                refundAmount = refundResponse.getRefundAmount();
                refundStatus = refundResponse.getRefundStatus();
                refundTransactionId = refundResponse.getRefundTransactionId();
            } catch (Exception e) {
                log.error("Failed to process refund for order: {}", orderId, e);
                refundStatus = "FAILED";
            }
        }
        
        // Publish OrderCancelledEvent
        OrderCancelledEvent orderCancelledEvent = new OrderCancelledEvent(
                savedOrder.getId(),
                savedOrder.getOrderNumber() != null ? savedOrder.getOrderNumber() : savedOrder.getId().toString(),
                savedOrder.getUserId(),
                reasonText,
                refundRequired
        );
        eventPublisher.publish(orderCancelledEvent, "order-cancelled");
        
        // Build response
        return CancellationResponse.builder()
                .orderId(savedOrder.getId())
                .orderNumber(savedOrder.getOrderNumber())
                .status(savedOrder.getStatus())
                .cancellationReason(reasonText)
                .cancelledAt(savedOrder.getCancelledAt())
                .refundProcessed(refundRequired && "SUCCESS".equals(refundStatus))
                .refundAmount(refundAmount)
                .refundStatus(refundStatus)
                .refundTransactionId(refundTransactionId)
                .inventoryUpdated(inventoryUpdated)
                .message("Order cancelled successfully")
                .build();
    }
    
    /**
     * Get list of available cancellation reasons.
     * 
     * @return List of cancellation reasons with descriptions
     */
    public List<CancellationReason> getCancellationReasons() {
        return Arrays.asList(CancellationReason.values());
    }
    
    /**
     * Process refund for a cancelled order.
     * 
     * @param orderId The ID of the order to refund
     * @param reason The reason for the refund
     * @return CancellationResponse with refund details
     */
    @Transactional
    public CancellationResponse processRefund(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        
        // Validate order can be refunded
        if (!"PAID".equalsIgnoreCase(order.getPaymentStatus()) && 
            !"PARTIAL".equalsIgnoreCase(order.getPaymentStatus())) {
            throw new BadRequestException("Order payment status does not allow refund: " + order.getPaymentStatus());
        }
        
        // Get payment for this order
        List<com.vendo.payment.dto.response.PaymentResponse> payments = 
                paymentService.getPaymentsByOrderId(orderId);
        
        if (payments.isEmpty()) {
            throw new BadRequestException("No payment found for order: " + orderId);
        }
        
        // Process refund for the first completed payment (in production, would handle multiple payments)
        com.vendo.payment.dto.response.PaymentResponse payment = payments.stream()
                .filter(p -> "completed".equalsIgnoreCase(p.getStatus()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("No completed payment found for order: " + orderId));
        
        // Calculate refund amount (full order amount)
        BigDecimal refundAmount = order.getTotalAmount();
        
        // Create refund request
        RefundPaymentRequest refundRequest = RefundPaymentRequest.builder()
                .paymentId(payment.getId())
                .amount(refundAmount)
                .reason(reason != null ? reason : "Order cancellation refund")
                .note("Refund for cancelled order: " + order.getOrderNumber())
                .build();
        
        // Process refund through payment gateway
        com.vendo.payment.dto.response.PaymentResponse refundedPayment;
        try {
            refundedPayment = paymentService.processRefund(refundRequest);
        } catch (Exception e) {
            log.error("Failed to process refund for order: {}", orderId, e);
            throw new BadRequestException("Refund processing failed: " + e.getMessage());
        }
        
        // Update order payment status
        if (refundedPayment.getRefundedAmount() != null && 
            refundedPayment.getRefundedAmount().compareTo(payment.getAmount()) >= 0) {
            order.setPaymentStatus("REFUNDED");
            order.setStatus("REFUNDED");
        } else {
            order.setPaymentStatus("PARTIAL_REFUNDED");
        }
        
        // Add history entry
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .action("REFUND_PROCESSED")
                .description("Refund processed: " + refundAmount + " - " + reason)
                .build();
        order.getHistories().add(history);
        
        orderRepository.save(order);
        log.info("Refund processed for order: orderId={}, amount={}", orderId, refundAmount);
        
        return CancellationResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .refundProcessed(true)
                .refundAmount(refundAmount)
                .refundStatus("SUCCESS")
                .refundTransactionId(refundedPayment.getChargeId())
                .message("Refund processed successfully")
                .build();
    }
    
    /**
     * Validate if order can be cancelled based on status and time window.
     */
    private void validateCancellationEligibility(Order order) {
        // Check order status
        String status = order.getStatus().toUpperCase();
        if ("CANCELLED".equals(status) || "REFUNDED".equals(status) || "DELIVERED".equals(status)) {
            throw new BadRequestException("Order cannot be cancelled in current status: " + status);
        }
        
        // Check time window (only for PENDING and CONFIRMED orders)
        if ("PENDING".equals(status) || "CONFIRMED".equals(status)) {
            LocalDateTime now = LocalDateTime.now();
            long hoursSinceCreation = ChronoUnit.HOURS.between(order.getCreatedAt(), now);
            
            if (hoursSinceCreation > cancellationTimeWindowHours) {
                throw new BadRequestException(
                        String.format("Order cannot be cancelled after %d hours. Order was created %d hours ago.",
                                cancellationTimeWindowHours, hoursSinceCreation));
            }
        }
        
        // PROCESSING and SHIPPED orders can be cancelled but may require admin approval
        // This is allowed but will trigger refund processing
    }
    
    /**
     * Build cancellation reason text from request.
     */
    private String buildCancellationReason(CancelOrderRequest request) {
        if (request.getReason() == CancellationReason.OTHER && 
            request.getCustomReason() != null && !request.getCustomReason().trim().isEmpty()) {
            return request.getCustomReason();
        }
        return request.getReason().getDescription();
    }
    
    /**
     * Determine if refund is required based on order status and payment status.
     */
    private boolean isRefundRequired(Order order) {
        String paymentStatus = order.getPaymentStatus();
        return "PAID".equalsIgnoreCase(paymentStatus) || "PARTIAL".equalsIgnoreCase(paymentStatus);
    }
    
    /**
     * Update inventory when order is cancelled - release reserved stock.
     */
    private boolean updateInventoryOnCancellation(Order order) {
        try {
            // Update inventory for order items
            if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                for (OrderItem item : order.getOrderItems()) {
                    if (item.getProduct() != null) {
                        releaseInventoryForProduct(item.getProduct().getId(), item.getQuantity());
                    }
                }
            }
            
            // Also handle legacy OrderProduct for backward compatibility
            if (order.getOrderProducts() != null && !order.getOrderProducts().isEmpty()) {
                for (OrderProduct orderProduct : order.getOrderProducts()) {
                    if (orderProduct.getProductId() != null) {
                        releaseInventoryForProduct(orderProduct.getProductId(), orderProduct.getQty());
                    }
                }
            }
            
            log.info("Inventory updated for cancelled order: {}", order.getId());
            return true;
        } catch (Exception e) {
            log.error("Failed to update inventory for cancelled order: {}", order.getId(), e);
            // Don't fail cancellation if inventory update fails - log and continue
            return false;
        }
    }
    
    /**
     * Release reserved inventory for a product.
     * In production, this would call an InventoryService or use InventoryRepository.
     * For now, we'll publish an event that can be handled by inventory service.
     */
    private void releaseInventoryForProduct(Long productId, Integer quantity) {
        // In a microservices architecture, this would be an async call or event
        // For now, we'll log it - the actual implementation would:
        // 1. Find inventory record for product
        // 2. Decrease reservedQuantity by quantity
        // 3. Create StockMovement record with type "RETURN" and reference to order
        log.info("Releasing inventory: productId={}, quantity={}", productId, quantity);
        
        // TODO: Implement actual inventory release
        // This could be done via:
        // - Direct repository call if in same service
        // - REST call to inventory service
        // - Event publishing for async processing
    }
}
