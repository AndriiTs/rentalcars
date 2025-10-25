package com.rentalcar.rental.command.handler;

import com.rentalcar.customer.domain.model.Customer;
import com.rentalcar.customer.domain.repository.CustomerRepository;
import com.rentalcar.fleet.domain.model.Car;
import com.rentalcar.fleet.domain.repository.CarRepository;
import com.rentalcar.rental.command.api.*;
import com.rentalcar.rental.domain.event.*;
import com.rentalcar.rental.domain.model.Rental;
import com.rentalcar.rental.domain.model.RentalPeriod;
import com.rentalcar.rental.domain.repository.RentalRepository;
import com.rentalcar.rental.domain.service.RentalPricingService;
import com.rentalcar.shared.domain.Money;
import com.rentalcar.shared.messaging.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * COMMAND HANDLER: RentalCommandHandler
 *
 * Handles commands for the Rental aggregate.
 * This is the WRITE SIDE of CQRS.
 *
 * Responsibilities:
 * 1. Validate command
 * 2. Load/create aggregates
 * 3. Execute business logic
 * 4. Persist changes
 * 5. Publish domain events to Kafka
 *
 * Pattern: Command Handler in CQRS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RentalCommandHandler {

    private final RentalRepository rentalRepository;
    private final CustomerRepository customerRepository;
    private final CarRepository carRepository;
    private final RentalPricingService pricingService;
    private final EventPublisher eventPublisher;

    /**
     * Handle CreateRentalCommand
     */
    @Transactional
    public String handle(CreateRentalCommand command) {
        log.info("Handling CreateRentalCommand: {}", command);

        // Validate customer
        Customer customer = customerRepository.findById(command.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + command.getCustomerId()));

        if (!customer.isVerified()) {
            throw new IllegalStateException("Customer must be verified before renting");
        }

        if (!customer.isEligibleToRent()) {
            throw new IllegalStateException("Customer is not eligible to rent (must be 21+)");
        }

        // Validate car
        Car car = carRepository.findById(command.getCarId())
                .orElseThrow(() -> new IllegalArgumentException("Car not found: " + command.getCarId()));

        if (!car.isAvailable()) {
            throw new IllegalStateException("Car is not available for rental");
        }

        // Create rental period
        RentalPeriod period = RentalPeriod.of(command.getStartDate(), command.getEndDate());

        // Calculate pricing
        Money totalCost = pricingService.calculateTotalCost(car.getDailyRate(), period);

        // Create rental aggregate
        Rental rental = Rental.create(
                command.getCustomerId(),
                command.getCarId(),
                period,
                totalCost
        );

        // Mark car as rented
        car.markAsRented();

        // Persist
        rentalRepository.save(rental);
        carRepository.save(car);

        // Publish event
        RentalCreatedEvent event = new RentalCreatedEvent(
                rental.getRentalId(),
                rental.getCustomerId(),
                rental.getCarId(),
                period.getStartDate(),
                period.getEndDate(),
                totalCost.getAmount(),
                totalCost.getCurrency()
        );

        eventPublisher.publish(event);

        log.info("Rental created successfully: {}", rental.getRentalId());
        return rental.getRentalId();
    }

    /**
     * Handle StartRentalCommand
     */
    @Transactional
    public void handle(StartRentalCommand command) {
        log.info("Handling StartRentalCommand: {}", command);

        Rental rental = rentalRepository.findById(command.getRentalId())
                .orElseThrow(() -> new IllegalArgumentException("Rental not found: " + command.getRentalId()));

        // Execute domain logic
        rental.startRental(command.getStartOdometer());

        // Persist
        rentalRepository.save(rental);

        // Publish event
        RentalStartedEvent event = new RentalStartedEvent(
                rental.getRentalId(),
                command.getStartOdometer(),
                LocalDateTime.now()
        );

        eventPublisher.publish(event);

        log.info("Rental started successfully: {}", rental.getRentalId());
    }

    /**
     * Handle CompleteRentalCommand
     */
    @Transactional
    public void handle(CompleteRentalCommand command) {
        log.info("Handling CompleteRentalCommand: {}", command);

        Rental rental = rentalRepository.findById(command.getRentalId())
                .orElseThrow(() -> new IllegalArgumentException("Rental not found: " + command.getRentalId()));

        // Get car to update its status
        Car car = carRepository.findById(rental.getCarId())
                .orElseThrow(() -> new IllegalArgumentException("Car not found: " + rental.getCarId()));

        // Execute domain logic
        rental.completeRental(command.getEndOdometer());
        car.updateOdometer(command.getEndOdometer());
        car.markAsAvailable();

        // Persist
        rentalRepository.save(rental);
        carRepository.save(car);

        // Publish event
        RentalCompletedEvent event = new RentalCompletedEvent(
                rental.getRentalId(),
                rental.getCarId(),
                command.getEndOdometer(),
                LocalDateTime.now()
        );

        eventPublisher.publish(event);

        log.info("Rental completed successfully: {}", rental.getRentalId());
    }

    /**
     * Handle CancelRentalCommand
     */
    @Transactional
    public void handle(CancelRentalCommand command) {
        log.info("Handling CancelRentalCommand: {}", command);

        Rental rental = rentalRepository.findById(command.getRentalId())
                .orElseThrow(() -> new IllegalArgumentException("Rental not found: " + command.getRentalId()));

        // Get car to make it available again
        Car car = carRepository.findById(rental.getCarId())
                .orElseThrow(() -> new IllegalArgumentException("Car not found: " + rental.getCarId()));

        // Execute domain logic
        rental.cancel();
        car.markAsAvailable();

        // Persist
        rentalRepository.save(rental);
        carRepository.save(car);

        // Publish event
        RentalCancelledEvent event = new RentalCancelledEvent(
                rental.getRentalId(),
                rental.getCarId(),
                command.getReason()
        );

        eventPublisher.publish(event);

        log.info("Rental cancelled successfully: {}", rental.getRentalId());
    }
}
