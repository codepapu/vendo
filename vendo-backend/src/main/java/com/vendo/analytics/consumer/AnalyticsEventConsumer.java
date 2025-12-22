package com.vendo.analytics.consumer;

import com.vendo.analytics.entity.AnalyticsEvent;
import com.vendo.analytics.repository.AnalyticsEventRepository;
import com.vendo.common.event.BaseEvent;
import com.vendo.common.event.order.OrderCancelledEvent;
import com.vendo.common.event.order.OrderCreatedEvent;
import com.vendo.common.event.order.OrderUpdatedEvent;
import com.vendo.common.event.payment.PaymentFailedEvent;
import com.vendo.common.event.payment.PaymentProcessedEvent;
import com.vendo.common.event.payment.PaymentRefundedEvent;
import com.vendo.common.event.product.ProductCreatedEvent;
import com.vendo.common.event.product.ProductStockUpdatedEvent;
import com.vendo.common.event.product.ProductUpdatedEvent;
import com.vendo.common.event.user.UserCreatedEvent;
import com.vendo.common.event.user.UserUpdatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Event consumer for tracking all domain events in analytics.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsEventConsumer {
    
    private final AnalyticsEventRepository analyticsEventRepository;
    private final ObjectMapper objectMapper;
    
    @KafkaListener(topics = "order-created", groupId = "analytics-service")
    public void handleOrderCreated(@Payload OrderCreatedEvent event,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   Acknowledgment acknowledgment) {
        try {
            log.debug("Tracking OrderCreatedEvent: orderId={}", event.getOrderId());
            trackEvent("order.created", event.getUserId(), event);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error tracking OrderCreatedEvent: orderId={}", event.getOrderId(), e);
        }
    }
    
    @KafkaListener(topics = "order-updated", groupId = "analytics-service")
    public void handleOrderUpdated(@Payload OrderUpdatedEvent event,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   Acknowledgment acknowledgment) {
        try {
            log.debug("Tracking OrderUpdatedEvent: orderId={}", event.getOrderId());
            trackEvent("order.updated", event.getUserId(), event);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error tracking OrderUpdatedEvent: orderId={}", event.getOrderId(), e);
        }
    }
    
    @KafkaListener(topics = "order-cancelled", groupId = "analytics-service")
    public void handleOrderCancelled(@Payload OrderCancelledEvent event,
                                     @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                     Acknowledgment acknowledgment) {
        try {
            log.debug("Tracking OrderCancelledEvent: orderId={}", event.getOrderId());
            trackEvent("order.cancelled", event.getUserId(), event);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error tracking OrderCancelledEvent: orderId={}", event.getOrderId(), e);
        }
    }
    
    @KafkaListener(topics = "payment-processed", groupId = "analytics-service")
    public void handlePaymentProcessed(@Payload PaymentProcessedEvent event,
                                      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                      Acknowledgment acknowledgment) {
        try {
            log.debug("Tracking PaymentProcessedEvent: paymentId={}", event.getPaymentId());
            trackEvent("payment.processed", event.getUserId(), event);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error tracking PaymentProcessedEvent: paymentId={}", event.getPaymentId(), e);
        }
    }
    
    @KafkaListener(topics = "payment-failed", groupId = "analytics-service")
    public void handlePaymentFailed(@Payload PaymentFailedEvent event,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                    Acknowledgment acknowledgment) {
        try {
            log.debug("Tracking PaymentFailedEvent: paymentId={}", event.getPaymentId());
            trackEvent("payment.failed", event.getUserId(), event);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error tracking PaymentFailedEvent: paymentId={}", event.getPaymentId(), e);
        }
    }
    
    @KafkaListener(topics = "payment-refunded", groupId = "analytics-service")
    public void handlePaymentRefunded(@Payload PaymentRefundedEvent event,
                                      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                      Acknowledgment acknowledgment) {
        try {
            log.debug("Tracking PaymentRefundedEvent: paymentId={}", event.getPaymentId());
            trackEvent("payment.refunded", event.getUserId(), event);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error tracking PaymentRefundedEvent: paymentId={}", event.getPaymentId(), e);
        }
    }
    
    @KafkaListener(topics = "user-created", groupId = "analytics-service")
    public void handleUserCreated(@Payload UserCreatedEvent event,
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                  Acknowledgment acknowledgment) {
        try {
            log.debug("Tracking UserCreatedEvent: userId={}", event.getUserId());
            trackEvent("user.created", event.getUserId(), event);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error tracking UserCreatedEvent: userId={}", event.getUserId(), e);
        }
    }
    
    @KafkaListener(topics = "user-updated", groupId = "analytics-service")
    public void handleUserUpdated(@Payload UserUpdatedEvent event,
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                  Acknowledgment acknowledgment) {
        try {
            log.debug("Tracking UserUpdatedEvent: userId={}", event.getUserId());
            trackEvent("user.updated", event.getUserId(), event);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error tracking UserUpdatedEvent: userId={}", event.getUserId(), e);
        }
    }
    
    @KafkaListener(topics = "product-created", groupId = "analytics-service")
    public void handleProductCreated(@Payload ProductCreatedEvent event,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                    Acknowledgment acknowledgment) {
        try {
            log.debug("Tracking ProductCreatedEvent: productId={}", event.getProductId());
            trackEvent("product.created", null, event);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error tracking ProductCreatedEvent: productId={}", event.getProductId(), e);
        }
    }
    
    @KafkaListener(topics = "product-updated", groupId = "analytics-service")
    public void handleProductUpdated(@Payload ProductUpdatedEvent event,
                                     @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                     Acknowledgment acknowledgment) {
        try {
            log.debug("Tracking ProductUpdatedEvent: productId={}", event.getProductId());
            trackEvent("product.updated", null, event);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error tracking ProductUpdatedEvent: productId={}", event.getProductId(), e);
        }
    }
    
    @KafkaListener(topics = "product-stock-updated", groupId = "analytics-service")
    public void handleProductStockUpdated(@Payload ProductStockUpdatedEvent event,
                                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                         Acknowledgment acknowledgment) {
        try {
            log.debug("Tracking ProductStockUpdatedEvent: productId={}", event.getProductId());
            trackEvent("product.stock.updated", null, event);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error tracking ProductStockUpdatedEvent: productId={}", event.getProductId(), e);
        }
    }
    
    /**
     * Helper method to track events in analytics database.
     */
    private void trackEvent(String eventType, Long userId, BaseEvent event) {
        try {
            String propertiesJson = objectMapper.writeValueAsString(event);
            
            AnalyticsEvent analyticsEvent = AnalyticsEvent.builder()
                    .eventType(eventType)
                    .userId(userId)
                    .sessionId(event.getCorrelationId())
                    .properties(propertiesJson)
                    .build();
            
            analyticsEventRepository.save(analyticsEvent);
        } catch (Exception e) {
            log.error("Error saving analytics event: eventType={}", eventType, e);
        }
    }
}
