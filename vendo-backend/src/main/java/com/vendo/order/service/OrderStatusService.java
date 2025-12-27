package com.vendo.order.service;

import com.vendo.common.event.EventPublisher;
import com.vendo.common.event.order.OrderUpdatedEvent;
import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.order.entity.Order;
import com.vendo.order.entity.OrderHistory;
import com.vendo.order.repository.OrderRepository;
import com.vendo.order.repository.OrderHistoryRepository;
import com.vendo.common.notification.NotificationService;
import com.vendo.retailer.service.RetailerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStatusService {
    
    // Define valid status transitions for each order type
    private static final Map<String, Map<String, String>> STATUS_WORKFLOWS = new HashMap<>();
    
    static {
        // B2C workflow
        Map<String, String> b2cWorkflow = new HashMap<>();
        b2cWorkflow.put("PENDING", "CONFIRMED");
        b2cWorkflow.put("CONFIRMED", "PROCESSING");
        b2cWorkflow.put("PROCESSING", "SHIPPED");
        b2cWorkflow.put("SHIPPED", "DELIVERED");
        STATUS_WORKFLOWS.put("B2C", b2cWorkflow);
        
        // B2B workflow
        Map<String, String> b2bWorkflow = new HashMap<>();
        b2bWorkflow.put("PENDING", "PO_APPROVED");
        b2bWorkflow.put("PO_APPROVED", "CONFIRMED");
        b2bWorkflow.put("CONFIRMED", "PROCESSING");
        b2bWorkflow.put("PROCESSING", "SHIPPED");
        b2bWorkflow.put("SHIPPED", "DELIVERED");
        STATUS_WORKFLOWS.put("B2B", b2bWorkflow);
        
        // Retailer workflow
        Map<String, String> retailerWorkflow = new HashMap<>();
        retailerWorkflow.put("PENDING", "CONFIRMED");
        retailerWorkflow.put("CONFIRMED", "PROCESSING");
        retailerWorkflow.put("PROCESSING", "SHIPPED");
        retailerWorkflow.put("SHIPPED", "DELIVERED");
        STATUS_WORKFLOWS.put("RETAILER", retailerWorkflow);
    }
    
    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final EventPublisher eventPublisher;
    private final NotificationService notificationService;
    private final RetailerService retailerService;
    
    /**
     * Update order status with validation
     */
    @Transactional
    public Order updateOrderStatus(Long orderId, String newStatus, String description, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        
        String currentStatus = order.getStatus();
        String orderType = order.getOrderType() != null ? order.getOrderType() : "B2C";
        
        // Validate status transition
        validateStatusTransition(orderType, currentStatus, newStatus);
        
        // Update order status
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        
        // Create order history
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .status(newStatus.toLowerCase())
                .description(description != null ? description : 
                        String.format("Status changed from %s to %s", currentStatus, newStatus))
                .build();
        
        order.getHistories().add(history);
        
        Order savedOrder = orderRepository.save(order);
        log.info("Order status updated: Order ID={}, {} -> {}", orderId, currentStatus, newStatus);
        
        // Evict retailer dashboard cache if order has retailer items
        if (savedOrder.getOrderItems() != null && !savedOrder.getOrderItems().isEmpty()) {
            savedOrder.getOrderItems().stream()
                    .filter(item -> item.getRetailer() != null)
                    .map(item -> item.getRetailer().getId())
                    .distinct()
                    .forEach(retailerId -> {
                        try {
                            retailerService.evictDashboardCache(retailerId);
                        } catch (Exception e) {
                            log.warn("Failed to evict dashboard cache for retailer {}: {}", retailerId, e.getMessage());
                        }
                    });
        }
        
        // Publish event
        OrderUpdatedEvent event = new OrderUpdatedEvent(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getUser() != null ? savedOrder.getUser().getId() : null,
                currentStatus,
                newStatus,
                savedOrder.getTotalAmount(),
                savedOrder.getCurrency()
        );
        eventPublisher.publish(event, "order-updated");
        
        // Send notification
        if (savedOrder.getUser() != null) {
            String message = String.format(
                    "Your order %s status has been updated to %s",
                    savedOrder.getOrderNumber(),
                    newStatus
            );
            
            notificationService.sendNotification(
                    savedOrder.getUser().getId(),
                    "Order Status Update",
                    message,
                    com.vendo.common.notification.NotificationPriority.MEDIUM
            );
        }
        
        return savedOrder;
    }
    
    /**
     * Get next valid status for order
     */
    @Transactional(readOnly = true)
    public String getNextStatus(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        
        String orderType = order.getOrderType() != null ? order.getOrderType() : "B2C";
        String currentStatus = order.getStatus();
        
        Map<String, String> workflow = STATUS_WORKFLOWS.get(orderType);
        if (workflow == null) {
            return null;
        }
        
        return workflow.get(currentStatus);
    }
    
    /**
     * Check if status transition is valid
     */
    private void validateStatusTransition(String orderType, String currentStatus, String newStatus) {
        // Allow cancellation and refund from any status
        if ("CANCELLED".equals(newStatus) || "REFUNDED".equals(newStatus)) {
            return;
        }
        
        Map<String, String> workflow = STATUS_WORKFLOWS.get(orderType);
        if (workflow == null) {
            throw new BadRequestException("Invalid order type: " + orderType);
        }
        
        String expectedNextStatus = workflow.get(currentStatus);
        if (expectedNextStatus == null && !newStatus.equals(currentStatus)) {
            throw new BadRequestException(
                    String.format("Invalid status transition from %s to %s for order type %s",
                            currentStatus, newStatus, orderType));
        }
        
        if (expectedNextStatus != null && !newStatus.equals(expectedNextStatus) && !newStatus.equals(currentStatus)) {
            throw new BadRequestException(
                    String.format("Invalid status transition. Expected %s but got %s",
                            expectedNextStatus, newStatus));
        }
    }
}

