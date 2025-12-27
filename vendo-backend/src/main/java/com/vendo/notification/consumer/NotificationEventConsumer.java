package com.vendo.notification.consumer;

import com.vendo.common.event.BaseEvent;
import com.vendo.common.event.order.OrderCancelledEvent;
import com.vendo.common.event.order.OrderCreatedEvent;
import com.vendo.common.event.order.OrderUpdatedEvent;
import com.vendo.common.event.payment.PaymentFailedEvent;
import com.vendo.common.event.payment.PaymentProcessedEvent;
import com.vendo.common.event.payment.PaymentRefundedEvent;
import com.vendo.common.event.user.UserCreatedEvent;
import com.vendo.notification.dto.request.SendNotificationRequest;
import com.vendo.notification.enums.NotificationType;
import com.vendo.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Event consumer for handling domain events and triggering notifications.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {
    
    private final NotificationService notificationService;
    
    @KafkaListener(topics = "order-created", groupId = "notification-service")
    public void handleOrderCreated(@Payload OrderCreatedEvent event,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   Acknowledgment acknowledgment) {
        try {
            log.info("Received OrderCreatedEvent: orderId={}, userId={}", event.getOrderId(), event.getUserId());
            
            // Send order confirmation notification via all enabled channels based on user preferences
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("orderId", event.getOrderId());
            orderData.put("orderNumber", event.getOrderNumber());
            orderData.put("totalAmount", event.getTotalAmount());
            
            SendNotificationRequest notificationRequest = SendNotificationRequest.builder()
                    .userId(event.getUserId())
                    .notificationType(NotificationType.ORDER_CONFIRMED)
                    .channel("in_app") // Will also send via other enabled channels based on preferences
                    .title("Order Confirmed")
                    .message(String.format("Your order #%s has been confirmed. Total: %s", 
                            event.getOrderNumber(), event.getTotalAmount()))
                    .data(orderData)
                    .build();
            
            notificationService.sendNotification(notificationRequest);
            
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent: orderId={}", event.getOrderId(), e);
            // In production, would implement retry logic or dead letter queue
        }
    }
    
    @KafkaListener(topics = "order-updated", groupId = "notification-service")
    public void handleOrderUpdated(@Payload OrderUpdatedEvent event,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   Acknowledgment acknowledgment) {
        try {
            log.info("Received OrderUpdatedEvent: orderId={}, status={}->{}", 
                    event.getOrderId(), event.getOldStatus(), event.getNewStatus());
            
            // Send notification for significant status changes
            if ("completed".equals(event.getNewStatus()) || 
                "shipped".equals(event.getNewStatus()) ||
                "cancelled".equals(event.getNewStatus())) {
                
                String title = switch (event.getNewStatus()) {
                    case "completed" -> "Order Completed";
                    case "shipped" -> "Order Shipped";
                    case "cancelled" -> "Order Cancelled";
                    default -> "Order Status Updated";
                };
                
                NotificationType notificationType = switch (event.getNewStatus()) {
                    case "shipped" -> NotificationType.ORDER_SHIPPED;
                    case "cancelled" -> NotificationType.ORDER_CANCELLED;
                    case "completed" -> NotificationType.ORDER_DELIVERED;
                    default -> NotificationType.ORDER_CONFIRMED;
                };
                
                Map<String, Object> orderData = new HashMap<>();
                orderData.put("orderId", event.getOrderId());
                orderData.put("orderNumber", event.getOrderNumber());
                orderData.put("status", event.getNewStatus());
                
                SendNotificationRequest notificationRequest = SendNotificationRequest.builder()
                        .userId(event.getUserId())
                        .notificationType(notificationType)
                        .channel("in_app")
                        .title(title)
                        .message(String.format("Your order #%s status has been updated to %s", 
                                event.getOrderNumber(), event.getNewStatus()))
                        .data(orderData)
                        .build();
                
                notificationService.sendNotification(notificationRequest);
            }
            
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing OrderUpdatedEvent: orderId={}", event.getOrderId(), e);
        }
    }
    
    @KafkaListener(topics = "order-cancelled", groupId = "notification-service")
    public void handleOrderCancelled(@Payload OrderCancelledEvent event,
                                     @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                     Acknowledgment acknowledgment) {
        try {
            log.info("Received OrderCancelledEvent: orderId={}, refundRequired={}", 
                    event.getOrderId(), event.getRefundRequired());
            
            String message = String.format("Your order #%s has been cancelled. %s", 
                    event.getOrderNumber(),
                    event.getRefundRequired() ? "A refund will be processed." : "");
            
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("orderId", event.getOrderId());
            orderData.put("orderNumber", event.getOrderNumber());
            orderData.put("refundRequired", event.getRefundRequired());
            
            SendNotificationRequest notificationRequest = SendNotificationRequest.builder()
                    .userId(event.getUserId())
                    .notificationType(NotificationType.ORDER_CANCELLED)
                    .channel("in_app")
                    .title("Order Cancelled")
                    .message(message)
                    .data(orderData)
                    .build();
            
            notificationService.sendNotification(notificationRequest);
            
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing OrderCancelledEvent: orderId={}", event.getOrderId(), e);
        }
    }
    
    @KafkaListener(topics = "payment-processed", groupId = "notification-service")
    public void handlePaymentProcessed(@Payload PaymentProcessedEvent event,
                                      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                      Acknowledgment acknowledgment) {
        try {
            log.info("Received PaymentProcessedEvent: paymentId={}, orderId={}", 
                    event.getPaymentId(), event.getOrderId());
            
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("paymentId", event.getPaymentId());
            paymentData.put("orderId", event.getOrderId());
            paymentData.put("amount", event.getAmount());
            paymentData.put("currency", event.getCurrency());
            
            SendNotificationRequest notificationRequest = SendNotificationRequest.builder()
                    .userId(event.getUserId())
                    .notificationType(NotificationType.PAYMENT_SUCCESS)
                    .channel("in_app")
                    .title("Payment Successful")
                    .message(String.format("Your payment of %s %s has been processed successfully.", 
                            event.getAmount(), event.getCurrency()))
                    .data(paymentData)
                    .build();
            
            notificationService.sendNotification(notificationRequest);
            
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing PaymentProcessedEvent: paymentId={}", event.getPaymentId(), e);
        }
    }
    
    @KafkaListener(topics = "payment-failed", groupId = "notification-service")
    public void handlePaymentFailed(@Payload PaymentFailedEvent event,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                    Acknowledgment acknowledgment) {
        try {
            log.info("Received PaymentFailedEvent: paymentId={}, orderId={}", 
                    event.getPaymentId(), event.getOrderId());
            
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("paymentId", event.getPaymentId());
            paymentData.put("orderId", event.getOrderId());
            paymentData.put("errorCode", event.getErrorCode());
            paymentData.put("failureReason", event.getFailureReason());
            
            SendNotificationRequest notificationRequest = SendNotificationRequest.builder()
                    .userId(event.getUserId())
                    .notificationType(NotificationType.PAYMENT_FAILED)
                    .channel("in_app")
                    .title("Payment Failed")
                    .message(String.format("Your payment failed: %s. Please try again.", 
                            event.getFailureReason()))
                    .data(paymentData)
                    .build();
            
            notificationService.sendNotification(notificationRequest);
            
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing PaymentFailedEvent: paymentId={}", event.getPaymentId(), e);
        }
    }
    
    @KafkaListener(topics = "payment-refunded", groupId = "notification-service")
    public void handlePaymentRefunded(@Payload PaymentRefundedEvent event,
                                      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                      Acknowledgment acknowledgment) {
        try {
            log.info("Received PaymentRefundedEvent: paymentId={}, refundAmount={}", 
                    event.getPaymentId(), event.getRefundAmount());
            
            Map<String, Object> refundData = new HashMap<>();
            refundData.put("paymentId", event.getPaymentId());
            refundData.put("refundId", event.getRefundId());
            refundData.put("orderId", event.getOrderId());
            refundData.put("refundAmount", event.getRefundAmount());
            refundData.put("currency", event.getCurrency());
            
            SendNotificationRequest notificationRequest = SendNotificationRequest.builder()
                    .userId(event.getUserId())
                    .notificationType(NotificationType.ORDER_REFUNDED)
                    .channel("in_app")
                    .title("Refund Processed")
                    .message(String.format("A refund of %s %s has been processed to your account.", 
                            event.getRefundAmount(), event.getCurrency()))
                    .data(refundData)
                    .build();
            
            notificationService.sendNotification(notificationRequest);
            
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing PaymentRefundedEvent: paymentId={}", event.getPaymentId(), e);
        }
    }
    
    @KafkaListener(topics = "user-created", groupId = "notification-service")
    public void handleUserCreated(@Payload UserCreatedEvent event,
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                  Acknowledgment acknowledgment) {
        try {
            log.info("Received UserCreatedEvent: userId={}, email={}", event.getUserId(), event.getEmail());
            
            // Send welcome notification
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", event.getUserId());
            userData.put("email", event.getEmail());
            
            SendNotificationRequest notificationRequest = SendNotificationRequest.builder()
                    .userId(event.getUserId())
                    .notificationType(NotificationType.GENERAL)
                    .channel("in_app")
                    .title("Welcome!")
                    .message(String.format("Welcome to Nexus, %s! We're excited to have you.", 
                            event.getFirstName() != null ? event.getFirstName() : "there"))
                    .data(userData)
                    .build();
            
            notificationService.sendNotification(notificationRequest);
            
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing UserCreatedEvent: userId={}", event.getUserId(), e);
        }
    }
}
