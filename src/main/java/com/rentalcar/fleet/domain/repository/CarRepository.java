package com.rentalcar.fleet.domain.repository;

import com.rentalcar.fleet.domain.model.AvailabilityStatus;
import com.rentalcar.fleet.domain.model.Car;

import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY: CarRepository
 *
 * Repository interface for Car aggregate.
 * This is a PURE DOMAIN interface with NO Spring Data dependencies.
 *
 * Follows DDD Repository pattern - provides collection-like interface for aggregates.
 * Implementation is in infrastructure layer (JpaCarRepository).
 */
public interface CarRepository {
    
    /**
     * Save or update a car
     */
    Car save(Car car);
    
    /**
     * Find car by its identifier
     */
    Optional<Car> findById(String carId);
    
    /**
     * Find car by VIN
     */
    Optional<Car> findByVin(String vin);
    
    /**
     * Find all cars with specific availability status
     */
    List<Car> findByAvailabilityStatus(AvailabilityStatus status);
    
    /**
     * Find all available cars
     */
    default List<Car> findAvailableCars() {
        return findByAvailabilityStatus(AvailabilityStatus.AVAILABLE);
    }
    
    /**
     * Find all cars
     */
    List<Car> findAll();
    
    /**
     * Check if car exists by VIN
     */
    boolean existsByVin(String vin);
}
