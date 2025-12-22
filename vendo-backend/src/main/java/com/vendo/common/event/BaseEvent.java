package com.vendo.common.event;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base class for all domain events in the system.
 * All events should extend this class to ensure consistency.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public abstract class BaseEvent {
    
    /**
     * Unique event identifier
     */
    private String eventId;
    
    /**
     * Event type (e.g., "order.created", "payment.processed")
     */
    private String eventType;
    
    /**
     * Event version for schema evolution
     */
    private String eventVersion;
    
    /**
     * Timestamp when the event occurred
     */
    private LocalDateTime timestamp;
    
    /**
     * Source service that generated the event
     */
    private String source;
    
    /**
     * Correlation ID for tracking related events
     */
    private String correlationId;
    
    /**
     * User ID associated with the event (if applicable)
     */
    private Long userId;
    
    /**
     * Creates a new event with default values
     */
    protected BaseEvent(String eventType, String source, Long userId) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.eventVersion = "1.0";
        this.timestamp = LocalDateTime.now();
        this.source = source;
        this.correlationId = UUID.randomUUID().toString();
        this.userId = userId;
    }
}
