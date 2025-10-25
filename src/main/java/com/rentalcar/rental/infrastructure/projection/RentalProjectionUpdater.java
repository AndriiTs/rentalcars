package com.rentalcar.rental.infrastructure.projection;

import com.rentalcar.customer.domain.model.Customer;
import com.rentalcar.customer.domain.repository.CustomerRepository;
import com.rentalcar.fleet.domain.model.Car;
import com.rentalcar.fleet.domain.repository.CarRepository;
import com.rentalcar.rental.domain.event.*;
import com.rentalcar.rental.domain.repository.RentalRepository;
import com.rentalcar.rental.query.model.RentalView;
import com.rentalcar.rental.query.repository.RentalViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * PROJECTION UPDATER: RentalProjectionUpdater
 * Kafka consumer that listens to rental events and updates read models.
 * This is the bridge between Write Side and Read Side in CQRS.
 * Flow:
 * 1. Command Handler executes command and publishes event to Kafka
 * 2. This consumer receives the event
 * 3. Fetches additional data from other aggregates
 * 4. Updates denormalized view in MongoDB
 * Pattern: Event-Driven Projection in CQRS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RentalProjectionUpdater {

    private final RentalViewRepository rentalViewRepository;
    private final CustomerRepository customerRepository;
    private final CarRepository carRepository;
    private final RentalRepository rentalRepository;

    /**
     * Handle RentalCreatedEvent
     * Creates a new read model with denormalized data
     */
    @KafkaListener(topics = "${kafka.topics.rental-events}", groupId = "rental-projection")
    public void handleRentalEvent(Object event) {
        log.info("Received event: {}", event.getClass().getSimpleName());

        try {
            switch (event) {
                case RentalCreatedEvent rentalCreatedEvent -> handleRentalCreated(rentalCreatedEvent);
                case RentalStartedEvent rentalStartedEvent -> handleRentalStarted(rentalStartedEvent);
                case RentalCompletedEvent rentalCompletedEvent -> handleRentalCompleted(rentalCompletedEvent);
                case RentalCancelledEvent rentalCancelledEvent -> handleRentalCancelled(rentalCancelledEvent);
                default -> {
                    log.warn("Unhandled event: {}", event.getClass().getSimpleName());
                }
            }
        } catch (Exception ex) {
            log.error("Error handling event: {}", event.getClass().getSimpleName(), ex);
            // In production: send to dead letter queue for retry
        }
    }

    private void handleRentalCreated(RentalCreatedEvent event) {
        log.info("Updating projection for RentalCreatedEvent: {}", event.getAggregateId());

        rentalRepository.findById(event.getAggregateId()).ifPresent(
                rental -> {
                    // Fetch additional data for denormalization
                    Customer customer = customerRepository.findById(rental.getCustomerId())
                            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

                    Car car = carRepository.findById(rental.getCarId())
                            .orElseThrow(() -> new IllegalArgumentException("Car not found"));

                    // Calculate duration
                    long durationDays = ChronoUnit.DAYS.between(event.getStartDate(), event.getEndDate());

                    // Build denormalized view
                    RentalView view = RentalView.builder()
                            .rentalId(event.getAggregateId())
                            // Customer data
                            .customerId(customer.getCustomerId())
                            .customerName(MessageFormat.format("{0} {1}", customer.getFirstName(), customer.getLastName()))
                            .customerEmail(customer.getContactInfo().getEmail())
                            .customerPhone(customer.getContactInfo().getPhone())
                            // Car data
                            .carId(car.getCarId())
                            .carMake(car.getSpecification().getMake())
                            .carModel(car.getSpecification().getModel())
                            .carYear(car.getSpecification().getYear())
                            .carCategory(car.getSpecification().getCategory().name())
                            .carLicensePlate(car.getLicensePlate())
                            // Rental details
                            .startDate(event.getStartDate())
                            .endDate(event.getEndDate())
                            .durationDays(durationDays)
                            // Pricing
                            .totalCostAmount(event.getTotalCostAmount())
                            .totalCostCurrency(event.getTotalCostCurrency())
                            .formattedTotalCost(String.format("%s %.2f",
                                    event.getTotalCostCurrency(), event.getTotalCostAmount()))
                            // Status
                            .status("RESERVED")
                            // Timestamps
                            .createdAt(event.getOccurredOn())
                            .lastUpdated(LocalDateTime.now())
                            .build();

                    rentalViewRepository.save(view);
                    log.info("Projection updated successfully for rental: {}", event.getAggregateId());
                });
    }

    private void handleRentalStarted(RentalStartedEvent event) {
        log.info("Updating projection for RentalStartedEvent: {}", event.getAggregateId());

        RentalView view = rentalViewRepository.findById(event.getAggregateId())
                .orElseThrow(() -> new IllegalArgumentException("Rental view not found"));

        view.setStatus("ACTIVE");
        view.setStartOdometer(event.getStartOdometer());
        view.setPickedUpAt(event.getPickedUpAt());
        view.setLastUpdated(LocalDateTime.now());

        rentalViewRepository.save(view);
        log.info("Projection updated successfully for rental: {}", event.getAggregateId());
    }

    private void handleRentalCompleted(RentalCompletedEvent event) {
        log.info("Updating projection for RentalCompletedEvent: {}", event.getAggregateId());

        RentalView view = rentalViewRepository.findById(event.getAggregateId())
                .orElseThrow(() -> new IllegalArgumentException("Rental view not found"));

        view.setStatus("COMPLETED");
        view.setEndOdometer(event.getEndOdometer());
        view.setReturnedAt(event.getReturnedAt());
        view.setLastUpdated(LocalDateTime.now());

        // Calculate total kilometers
        if (view.getStartOdometer() != null && event.getEndOdometer() != null) {
            view.setTotalKilometers(event.getEndOdometer() - view.getStartOdometer());
        }

        rentalViewRepository.save(view);
        log.info("Projection updated successfully for rental: {}", event.getAggregateId());
    }

    private void handleRentalCancelled(RentalCancelledEvent event) {
        log.info("Updating projection for RentalCancelledEvent: {}", event.getAggregateId());

        RentalView view = rentalViewRepository.findById(event.getAggregateId())
                .orElseThrow(() -> new IllegalArgumentException("Rental view not found"));

        view.setStatus("CANCELLED");
        view.setCancellationReason(event.getReason());
        view.setLastUpdated(LocalDateTime.now());

        rentalViewRepository.save(view);
        log.info("Projection updated successfully for rental: {}", event.getAggregateId());
    }
}
