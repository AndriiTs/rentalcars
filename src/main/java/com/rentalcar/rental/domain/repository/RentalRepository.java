package com.rentalcar.rental.domain.repository;

import com.rentalcar.rental.domain.model.Rental;
import com.rentalcar.rental.domain.model.RentalStatus;

import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY: RentalRepository
 *
 * Repository interface for Rental aggregate.
 * This is a PURE DOMAIN interface with NO Spring Data dependencies.
 *
 * Implementation is in infrastructure layer (JpaRentalRepository).
 * This keeps the domain layer framework-independent.
 */
public interface RentalRepository {
    
    /**
     * Save or update a rental
     */
    Rental save(Rental rental);
    
    /**
     * Find rental by identifier
     */
    Optional<Rental> findById(String rentalId);
    
    /**
     * Find all rentals for a specific customer
     */
    List<Rental> findByCustomerId(String customerId);
    
    /**
     * Find all rentals for a specific car
     */
    List<Rental> findByCarId(String carId);
    
    /**
     * Find all rentals with specific status
     */
    List<Rental> findByStatus(RentalStatus status);
    
    /**
     * Find all active rentals
     */
    default List<Rental> findActiveRentals() {
        return findByStatus(RentalStatus.ACTIVE);
    }
    
    /**
     * Find all rentals
     */
    List<Rental> findAll();
}
