package com.vendo.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;

/**
 * Centralized event publisher for all domain events.
 * Publishes events to Kafka topics.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {
    
    private final KafkaTemplate<String, BaseEvent> kafkaTemplate;
    
    /**
     * Publishes an event to the appropriate Kafka topic.
     * 
     * @param event The event to publish
     * @param topic The Kafka topic name
     */
    public void publish(BaseEvent event, String topic) {
        try {
            log.info("Publishing event: {} to topic: {}", event.getEventType(), topic);
            
            CompletableFuture<SendResult<String, BaseEvent>> future = 
                kafkaTemplate.send(topic, event.getEventId(), event);
            
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event: eventId={}, topic={}", 
                            event.getEventId(), topic, ex);
                } else {
                    log.info("Event published successfully: eventId={}, topic={}, partition={}, offset={}",
                            event.getEventId(), topic, 
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });
        } catch (Exception e) {
            log.error("Error publishing event: eventId={}, topic={}", 
                    event.getEventId(), topic, e);
        }
    }
    
    /**
     * Publishes an event using the event type as the topic name.
     * 
     * @param event The event to publish
     */
    public void publish(BaseEvent event) {
        String topic = event.getEventType().replace(".", "-");
        publish(event, topic);
    }
}
