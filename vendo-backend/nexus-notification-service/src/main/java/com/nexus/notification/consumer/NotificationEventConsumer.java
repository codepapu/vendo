package com.nexus.notification.consumer;

import com.nexus.common.event.BaseEvent;
import com.nexus.common.event.order.OrderCancelledEvent;
import com.nexus.common.event.order.OrderCreatedEvent;
import com.nexus.common.event.order.OrderUpdatedEvent;
import com.nexus.common.event.payment.PaymentFailedEvent;
import com.nexus.common.event.payment.PaymentProcessedEvent;
import com.nexus.common.event.payment.PaymentRefundedEvent;
import com.nexus.common.event.user.UserCreatedEvent;
import com.nexus.notification.dto.request.SendNotificationRequest;
import com.nexus.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            
            // Send order confirmation notification
            SendNotificationRequest notificationRequest = SendNotificationRequest.builder()
                    .userId(event.getUserId())
                    .type("in_app")
                    .title("Order Confirmed")
                    .message(String.format("Your order #%s has been confirmed. Total: %s", 
                            event.getOrderNumber(), event.getTotalAmount()))
                    .data(String.format("{\"orderId\":%d,\"orderNumber\":\"%s\"}", 
                            event.getOrderId(), event.getOrderNumber()))
                    .build();
            
            notificationService.sendNotification(notificationRequest);
            
            // Also send email notification
            SendNotificationRequest emailRequest = SendNotificationRequest.builder()
                    .userId(event.getUserId())
                    .type("email")
                    .title("Order Confirmation - " + event.getOrderNumber())
                    .message(String.format("Thank you for your order! Your order #%s has been confirmed.", 
                            event.getOrderNumber()))
                    .data(String.format("{\"orderId\":%d,\"orderNumber\":\"%s\"}", 
                            event.getOrderId(), event.getOrderNumber()))
                    .build();
            
            notificationService.sendNotification(emailRequest);
            
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
                
                SendNotificationRequest notificationRequest = SendNotificationRequest.builder()
                        .userId(event.getUserId())
                        .type("in_app")
                        .title(title)
                        .message(String.format("Your order #%s status has been updated to %s", 
                                event.getOrderNumber(), event.getNewStatus()))
                        .data(String.format("{\"orderId\":%d,\"orderNumber\":\"%s\",\"status\":\"%s\"}", 
                                event.getOrderId(), event.getOrderNumber(), event.getNewStatus()))
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
            
            SendNotificationRequest notificationRequest = SendNotificationRequest.builder()
                    .userId(event.getUserId())
                    .type("in_app")
                    .title("Order Cancelled")
                    .message(message)
                    .data(String.format("{\"orderId\":%d,\"orderNumber\":\"%s\",\"refundRequired\":%s}", 
                            event.getOrderId(), event.getOrderNumber(), event.getRefundRequired()))
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
            
            SendNotificationRequest notificationRequest = SendNotificationRequest.builder()
                    .userId(event.getUserId())
                    .type("in_app")
                    .title("Payment Successful")
                    .message(String.format("Your payment of %s %s has been processed successfully.", 
                            event.getAmount(), event.getCurrency()))
                    .data(String.format("{\"paymentId\":%d,\"orderId\":%d}", 
                            event.getPaymentId(), event.getOrderId()))
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
            
            SendNotificationRequest notificationRequest = SendNotificationRequest.builder()
                    .userId(event.getUserId())
                    .type("in_app")
                    .title("Payment Failed")
                    .message(String.format("Your payment failed: %s. Please try again.", 
                            event.getFailureReason()))
                    .data(String.format("{\"paymentId\":%d,\"orderId\":%d,\"errorCode\":\"%s\"}", 
                            event.getPaymentId(), event.getOrderId(), event.getErrorCode()))
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
            
            SendNotificationRequest notificationRequest = SendNotificationRequest.builder()
                    .userId(event.getUserId())
                    .type("in_app")
                    .title("Refund Processed")
                    .message(String.format("A refund of %s %s has been processed to your account.", 
                            event.getRefundAmount(), event.getCurrency()))
                    .data(String.format("{\"paymentId\":%d,\"refundId\":%d,\"orderId\":%d}", 
                            event.getPaymentId(), event.getRefundId(), event.getOrderId()))
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
            SendNotificationRequest notificationRequest = SendNotificationRequest.builder()
                    .userId(event.getUserId())
                    .type("in_app")
                    .title("Welcome!")
                    .message(String.format("Welcome to Nexus, %s! We're excited to have you.", 
                            event.getFirstName() != null ? event.getFirstName() : "there"))
                    .data(String.format("{\"userId\":%d}", event.getUserId()))
                    .build();
            
            notificationService.sendNotification(notificationRequest);
            
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing UserCreatedEvent: userId={}", event.getUserId(), e);
        }
    }
}
