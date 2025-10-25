package com.rentalcar.rental.application;

import com.rentalcar.customer.domain.model.Customer;
import com.rentalcar.customer.domain.repository.CustomerRepository;
import com.rentalcar.fleet.domain.model.Car;
import com.rentalcar.fleet.domain.repository.CarRepository;
import com.rentalcar.rental.domain.event.RentalCreated;
import com.rentalcar.rental.domain.model.Rental;
import com.rentalcar.rental.domain.model.RentalPeriod;
import com.rentalcar.rental.domain.repository.RentalRepository;
import com.rentalcar.rental.domain.service.RentalPricingService;
import com.rentalcar.shared.domain.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * APPLICATION SERVICE: RentalService
 *
 * Application service coordinates use cases across multiple aggregates.
 * It orchestrates domain objects but doesn't contain business logic itself.
 *
 * Key differences from Domain Service:
 * - Application Service: Orchestrates use cases, coordinates aggregates
 * - Domain Service: Contains domain logic that doesn't fit in entities
 *
 * This service coordinates interactions between:
 * - Rental aggregate (core domain)
 * - Car aggregate (fleet management)
 * - Customer aggregate (customer management)
 *
 * Note: Injects DOMAIN interfaces (RentalRepository), not infrastructure implementations.
 * Spring auto-wires the JPA implementation beans at runtime.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;
    private final RentalPricingService pricingService;
    
    /**
     * Create a new rental
     * 
     * This method coordinates multiple aggregates:
     * 1. Validates customer eligibility
     * 2. Validates car availability
     * 3. Calculates pricing
     * 4. Creates rental
     * 5. Updates car status
     * 6. Publishes domain event
     * 
     * @Transactional ensures consistency across aggregate updates
     */
    @Transactional
    public Rental createRental(String customerId, String carId, LocalDate startDate, LocalDate endDate) {
        log.info("Creating rental for customer: {}, car: {}", customerId, carId);
        
        // Load aggregates
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
        
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("Car not found: " + carId));
        
        // Validate business rules
        if (!customer.isEligibleToRent()) {
            throw new IllegalStateException("Customer is not eligible to rent: " + customer.getFullName());
        }
        
        if (!car.isAvailable()) {
            throw new IllegalStateException("Car is not available: " + car.getLicensePlate());
        }
        
        // Create value objects
        RentalPeriod rentalPeriod = RentalPeriod.of(startDate, endDate);
        
        // Calculate total cost using domain service
        Money totalCost = pricingService.calculateTotalCost(car.getDailyRate(), rentalPeriod);
        
        // Create rental aggregate
        Rental rental = Rental.create(customerId, carId, rentalPeriod, totalCost);
        rental = rentalRepository.save(rental);
        
        // Update car status (different aggregate)
        car.markAsRented();
        carRepository.save(car);
        
        // Publish domain event (in real app, would use Spring Events or message bus)
        publishRentalCreated(rental);
        
        log.info("Rental created successfully: {}", rental.getRentalId());
        return rental;
    }
    
    /**
     * Start rental (car pickup)
     */
    @Transactional
    public void startRental(String rentalId, Integer startOdometer) {
        log.info("Starting rental: {}", rentalId);
        
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Rental not found: " + rentalId));
        
        rental.startRental(startOdometer);
        rentalRepository.save(rental);
        
        log.info("Rental started successfully: {}", rentalId);
    }
    
    /**
     * Complete rental (car return)
     */
    @Transactional
    public void completeRental(String rentalId, Integer endOdometer) {
        log.info("Completing rental: {}", rentalId);
        
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Rental not found: " + rentalId));
        
        // Complete rental
        rental.completeRental(endOdometer);
        rentalRepository.save(rental);
        
        // Update car status and odometer
        Car car = carRepository.findById(rental.getCarId())
                .orElseThrow(() -> new IllegalArgumentException("Car not found: " + rental.getCarId()));
        
        car.updateOdometer(endOdometer);
        car.markAsAvailable();
        carRepository.save(car);
        
        log.info("Rental completed successfully: {}", rentalId);
    }
    
    /**
     * Cancel rental
     */
    @Transactional
    public void cancelRental(String rentalId) {
        log.info("Cancelling rental: {}", rentalId);
        
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Rental not found: " + rentalId));
        
        rental.cancel();
        rentalRepository.save(rental);
        
        // Make car available again
        Car car = carRepository.findById(rental.getCarId())
                .orElseThrow(() -> new IllegalArgumentException("Car not found: " + rental.getCarId()));
        
        car.markAsAvailable();
        carRepository.save(car);
        
        log.info("Rental cancelled successfully: {}", rentalId);
    }
    
    /**
     * Get customer's rental history
     */
    @Transactional(readOnly = true)
    public List<Rental> getCustomerRentals(String customerId) {
        return rentalRepository.findByCustomerId(customerId);
    }
    
    /**
     * Get all active rentals
     */
    @Transactional(readOnly = true)
    public List<Rental> getActiveRentals() {
        return rentalRepository.findActiveRentals();
    }
    
    /**
     * Publish domain event
     * In real application, would use Spring ApplicationEventPublisher or message broker
     */
    private void publishRentalCreated(Rental rental) {
        RentalCreated event = RentalCreated.of(
                rental.getRentalId(),
                rental.getCustomerId(),
                rental.getCarId()
        );
        
        log.info("Domain event published: {}", event);
        // In production: eventPublisher.publishEvent(event);
    }
}
