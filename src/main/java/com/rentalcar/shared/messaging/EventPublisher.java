package com.rentalcar.shared.messaging;

import com.rentalcar.shared.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Event Publisher - Publishes domain events to Kafka
 *
 * This component is responsible for publishing all domain events to Kafka topics.
 * Events are the primary mechanism for communication between bounded contexts
 * and for updating read models in CQRS architecture.
 *
 * Pattern: Event-Driven Architecture / Publish-Subscribe
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;

    /**
     * Publish a domain event to Kafka
     *
     * @param event The domain event to publish
     */
    public void publish(DomainEvent event) {
        String topic = event.getTopicName();
        String key = event.getAggregateId();

        log.info("Publishing event: {} to topic: {} with key: {}",
                event.getClass().getSimpleName(), topic, key);

        CompletableFuture<SendResult<String, DomainEvent>> future =
                kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event: {} to topic: {}",
                        event.getClass().getSimpleName(), topic, ex);
            } else {
                log.info("Event published successfully: {} to partition: {} with offset: {}",
                        event.getClass().getSimpleName(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    /**
     * Publish event and wait for confirmation (synchronous)
     *
     * Use this when you need to ensure the event was published before proceeding
     */
    public void publishAndWait(DomainEvent event) {
        String topic = event.getTopicName();
        String key = event.getAggregateId();

        try {
            log.info("Publishing event synchronously: {} to topic: {}",
                    event.getClass().getSimpleName(), topic);

            SendResult<String, DomainEvent> result =
                    kafkaTemplate.send(topic, key, event).get();

            log.info("Event published successfully: {} to partition: {} with offset: {}",
                    event.getClass().getSimpleName(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());

        } catch (Exception ex) {
            log.error("Failed to publish event synchronously: {}",
                    event.getClass().getSimpleName(), ex);
            throw new RuntimeException("Failed to publish event", ex);
        }
    }
}
