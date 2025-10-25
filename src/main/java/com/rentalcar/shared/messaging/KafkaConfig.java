package com.rentalcar.shared.messaging;

import com.rentalcar.shared.event.DomainEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka Configuration
 *
 * Configures Kafka topics and serialization settings.
 * Topics are created automatically on startup if they don't exist.
 */
@Configuration
public class KafkaConfig {

    @Value("${kafka.topics.rental-events}")
    private String rentalEventsTopic;

    @Value("${kafka.topics.fleet-events}")
    private String fleetEventsTopic;

    @Value("${kafka.topics.customer-events}")
    private String customerEventsTopic;

    /**
     * Rental Events Topic
     * Partitions: 3 (for scalability)
     * Replicas: 1 (single broker for development)
     */
    @Bean
    public NewTopic rentalEventsTopic() {
        return TopicBuilder.name(rentalEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Fleet Events Topic
     */
    @Bean
    public NewTopic fleetEventsTopic() {
        return TopicBuilder.name(fleetEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Customer Events Topic
     */
    @Bean
    public NewTopic customerEventsTopic() {
        return TopicBuilder.name(customerEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
