package com.rentalcar.shared.event;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base interface for all domain events in the system
 *
 * Domain events represent something that happened in the domain that
 * domain experts care about. They are immutable facts about the past.
 *
 * Events are published to Kafka and consumed by:
 * - Projection updaters (to update read models)
 * - Other bounded contexts (for eventual consistency)
 * - External systems (notifications, analytics, etc.)
 */
@Data
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public abstract class DomainEvent implements Serializable {

    private String eventId;
    private LocalDateTime occurredOn;
    private String aggregateId;
    private String aggregateType;
    private Long version;

    protected DomainEvent(String aggregateId, String aggregateType) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.version = 1L;
    }

    /**
     * Get the topic to which this event should be published
     */
    public abstract String getTopicName();
}
